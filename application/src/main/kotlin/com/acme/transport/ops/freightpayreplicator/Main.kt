package com.acme.transport.ops.freightpayreplicator

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Freightpay replicator replicators freightpay's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("FreightpayReplicator")
    var failed = 0
    try {
        try {
            pushOnboardingPlanId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-onboarding-plan-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateProductCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-product-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeVehicleGroupMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-vehicle-group-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorVenueStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-venue-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorRateLimitName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-rate-limit-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleAdmissionCalculationKind(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-admission-calculation-kind").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateTripLogsNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-trip-logs-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyFuelPolicyReportStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-fuel-policy-report-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleRouteBindingId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-route-binding-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryZoneWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-zone-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffTripStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-trip-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorBoxOfficeSaleNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-box-office-sale-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateGeoFenceWorkflowName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-geo-fence-workflow-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillShowCalculationStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-show-calculation-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayExpenseReportStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-expense-report-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleFanSessionsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-fan-sessions-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryDispatchAssignmentId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-dispatch-assignment-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayWorkOrderApprovalStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-work-order-approval-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayRegisteredModelsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-registered-models-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffTelemetryReadingImportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-telemetry-reading-import-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillReviewsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-reviews-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportRateLimitId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-rate-limit-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyAuditEventDecisionStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-audit-event-decision-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayFuelCardStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-fuel-card-status").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
