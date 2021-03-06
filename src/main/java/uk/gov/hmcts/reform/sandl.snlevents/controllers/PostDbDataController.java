package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Availability;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.AvailabilityRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.io.IOException;

import static org.springframework.http.ResponseEntity.ok;

@RestController()
@RequestMapping("/poc")
public class PostDbDataController {

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @PostMapping(path = "")
    public ResponseEntity postDbData() throws IOException {

        for (Room room : roomRepository.findAll()) {
            String msg = factsMapper.mapDbRoomToRuleJsonMessage(room);
            rulesService.postMessage(RulesService.UPSERT_ROOM, msg);
        }

        for (Person person : personRepository.findPeopleByPersonTypeEqualsIgnoreCase("judge")) {
            String msg = factsMapper.mapDbPersonToRuleJsonMessage(person);
            rulesService.postMessage(RulesService.UPSERT_JUDGE, msg);
        }

        for (Availability availability : availabilityRepository.findAll()) {
            String msg = factsMapper.mapDbAvailabilityToRuleJsonMessage(availability);
            rulesService.postMessage(RulesService.UPSERT_AVAILABILITY, msg);
        }

        for (Session session : sessionRepository.findAll()) {
            String msg = factsMapper.mapDbSessionToRuleJsonMessage(session);
            rulesService.postMessage(RulesService.UPSERT_SESSION, msg);
        }

        for (HearingPart hearingPart : hearingPartRepository.findAll()) {
            String msg = factsMapper.mapDbHearingPartToRuleJsonMessage(hearingPart);
            rulesService.postMessage(RulesService.UPSERT_HEARING_PART, msg);
        }

        return ok("OK");
    }
}
