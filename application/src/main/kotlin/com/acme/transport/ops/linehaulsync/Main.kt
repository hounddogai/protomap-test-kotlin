package com.acme.transport.ops.linehaulsync

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Linehaul sync syncs linehaul's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("LinehaulSync")
    var failed = 0
    try {
        try {
            mirrorAccessPoliciesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-access-policies-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffPatientHomeStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-patient-home-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardPayRunMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-pay-run-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncPreferenceId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-preference-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorRouteWorkflowNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-route-workflow-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillDriverHomeWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-driver-home-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorTelemetryReadingStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-telemetry-reading-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyDockName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-dock-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeBoxOfficeSalesUri(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-box-office-sales-uri").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeProofOfDeliveriesMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-proof-of-deliveries-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorAuditEventsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-audit-events-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportVehicleMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-vehicle-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushDriversMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-drivers-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDriverMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-driver-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayBudgetCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-budget-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleTripLogApprovalNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-trip-log-approval-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryAuditEventKind(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-audit-event-kind").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorGeoFenceMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-geo-fence-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffRouteMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-route-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushWorkOrderMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-work-order-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateGeoFenceReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-geo-fence-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeSeatMapStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-seat-map-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeSubscriptionsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-subscriptions-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayDriverWorkflowsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-driver-workflows-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncTelemetryReadingWorkflowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-telemetry-reading-workflow-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffDutyStatusDecisionStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-duty-status-decision-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyFuelPolicyMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-fuel-policy-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
