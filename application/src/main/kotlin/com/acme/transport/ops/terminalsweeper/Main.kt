package com.acme.transport.ops.terminalsweeper

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Terminal sweeper sweepers terminal's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("TerminalSweeper")
    var failed = 0
    try {
        try {
            pushDeliveryApprovalStatusNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-delivery-approval-status-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillPartCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-part-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryInspectionLifecycleSubject(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-inspection-lifecycle-subject").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorShowsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-shows-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncGetDocumentsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-get-documents-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayYardMoveReportMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-yard-move-report-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushIncidentMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-incident-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillAuditEventKind(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-audit-event-kind").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffCustomerSegmentsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-customer-segments-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayFuelPolicyVersionsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-fuel-policy-versions-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleDriverHomeStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-driver-home-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeVideoVisitsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-video-visits-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardConfigSetId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-config-set-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorPreferencesName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-preferences-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorVehicleCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-vehicle-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayTripLogApprovalStatusId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-trip-log-approval-status-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillStopRatesTitle(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-stop-rates-title").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncDriverLicenseReportMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-driver-license-report-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleAuditEventsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-audit-events-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushWorkOrderRatesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-work-order-rates-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardDriverHomesToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-driver-homes-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncDockId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-dock-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncFuelTransactionSource(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-fuel-transaction-source").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayCustomerSegmentStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-customer-segment-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardIncidentSubject(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-incident-subject").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
