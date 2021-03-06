locals {
  app_full_name = "${var.product}-${var.component}"
}
module "snl-events" {
  source               = "git@github.com:hmcts/moj-module-webapp"
  product              = "${var.product}-${var.component}"
  location             = "${var.location}"
  env                  = "${var.env}"
  ilbIp                = "${var.ilbIp}"
  is_frontend          = false
  subscription         = "${var.subscription}"
  additional_host_name = "${var.external_host_name}"

  app_settings = {
    SNL_EVENTS_DB_HOST = "${module.postgres-snl-events.host_name}"
    SNL_EVENTS_DB_PORT = "${module.postgres-snl-events.postgresql_listen_port}"
    SNL_EVENTS_DB_NAME = "${module.postgres-snl-events.postgresql_database}"
    SNL_EVENTS_DB_USERNAME = "${module.postgres-snl-events.user_name}"
    SNL_EVENTS_DB_PASSWORD = "${module.postgres-snl-events.postgresql_password}"
    SNL_EVENTS_DB_PARAMS = "?ssl=true"
    SNL_RULES_URL = "http://snl-rules-${var.env}.service.${data.terraform_remote_state.core_apps_compute.ase_name[0]}.internal"
  }

}

module "postgres-snl-events" {
  source              = "git@github.com:contino/moj-module-postgres?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "West Europe"
  env                 = "${var.env}"
  postgresql_user     = "snlevents"
}
