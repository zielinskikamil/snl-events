package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.CreateSession;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionInfo;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionWithHearings;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_FOR_JUDGE_DIARY_SQL;
import static uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SessionQueries.GET_SESSION_INFO_SQL;

@Service
public class SessionService {

    private final Function<Session, SessionInfo> sessionDbToSessionInfo =
        (Session s) -> new SessionInfo(
            s.getId(),
            s.getStart(),
            s.getDuration(),
            s.getPerson(),
            s.getRoom(),
            s.getCaseType()
        );

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private HearingPartRepository hearingPartRepository;
    @Autowired
    private UserTransactionService userTransactionService;

    public List getSessions() {
        return sessionRepository.findAll();
    }

    public Session getSessionById(UUID id) {
        return sessionRepository.findOne(id);
    }

    public List getSessionsFromDate(LocalDate localDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(localDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(localDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_INFO_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .getResultList();
    }

    public SessionWithHearings getSessionsWithHearingsForDates(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartDate(fromDate, toDate);

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(sessions);

        SessionWithHearings sessionsWithHearings = new SessionWithHearings();
        sessionsWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionsWithHearings.setHearingParts(hearingParts);

        return sessionsWithHearings;
    }

    public List<SessionInfo> getJudgeDiaryForDates(String judgeUsername, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        return entityManager.createQuery(GET_SESSION_FOR_JUDGE_DIARY_SQL, SessionInfo.class)
            .setParameter("dateStart", fromDate)
            .setParameter("dateEnd", toDate)
            .setParameter("judgeUsername", judgeUsername)
            .getResultList();
    }

    public SessionWithHearings getSessionJudgeDiaryForDates(String judgeUsername, LocalDate startDate,
                                                            LocalDate endDate) {
        OffsetDateTime fromDate = OffsetDateTime.of(startDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDate = OffsetDateTime.of(endDate, LocalTime.MAX, ZoneOffset.UTC);

        List<Session> sessions = sessionRepository.findSessionByStartBetweenAndPerson_UsernameEquals(fromDate,
            toDate, judgeUsername);

        List<HearingPart> hearingParts = hearingPartRepository.findBySessionIn(sessions);

        SessionWithHearings sessionWithHearings = new SessionWithHearings();
        sessionWithHearings.setSessions(
            sessions.stream().map(this.sessionDbToSessionInfo).collect(Collectors.toList())
        );
        sessionWithHearings.setHearingParts(hearingParts);

        return sessionWithHearings;
    }

    public Session save(Session session) {
        return sessionRepository.save(session);
    }

    public Session save(CreateSession createSession) {
        Session session = new Session();
        session.setId(createSession.getId());
        session.setDuration(createSession.getDuration());
        session.setStart(createSession.getStart());
        session.setCaseType(createSession.getCaseType());

        if (createSession.getRoomId() != null) {
            Room room = roomRepository.findOne(createSession.getRoomId());
            session.setRoom(room);
        }

        if (createSession.getPersonId() != null) {
            Person person = personRepository.findOne(createSession.getPersonId());
            session.setPerson(person);
        }

        return this.save(session);
    }

    @Transactional
    public UserTransaction saveWithTransaction(CreateSession createSession) {
        Session session = save(createSession);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        userTransactionDataList.add(new UserTransactionData("session", session.getId(), null, "insert", "delete", 0));

        return userTransactionService.startTransaction(createSession.getUserTransactionId(), userTransactionDataList);
    }
}
