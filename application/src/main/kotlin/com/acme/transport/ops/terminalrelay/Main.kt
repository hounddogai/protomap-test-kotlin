package com.acme.transport.ops.terminalrelay

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Terminal relay relays terminal's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("TerminalRelay")
    var failed = 0
    try {
        try {
            replayTelemetryReadingStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-telemetry-reading-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardVehicleEligibilityStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-vehicle-eligibility-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayDriverInboxesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-driver-inboxes-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillServiceScheduleTitle(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-service-schedule-title").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeWorkOrderWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-work-order-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportYardStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-yard-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleYardMoveStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-yard-move-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryDriverHomeMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-driver-home-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayTranslationKeyOptionsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-translation-key-options-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncYardId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-yard-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyFuelPolicyVersionsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-fuel-policy-versions-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffRateLimitsName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-rate-limits-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorWorkOrderOptionsCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-work-order-options-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayPatientMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-patient-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardDeliveryExceptionApprovalStatusId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-delivery-exception-approval-status-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillPendingRoutePlansMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-pending-route-plans-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffMediaAssetId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-media-asset-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncConcessionStandStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-concession-stand-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushSeatMapsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-seat-maps-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleConsentRecordSource(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-consent-record-source").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateDeliveriesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-deliveries-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyHouseholdMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-household-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardDriverHomeUpdatesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-driver-home-updates-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardJobOpeningId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-job-opening-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncVenueMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-venue-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportCertificateId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-certificate-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffPendingDriverLicensesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-pending-driver-licenses-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeGeoFencesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-geo-fences-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleDeliveryWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-delivery-workflow-id").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
