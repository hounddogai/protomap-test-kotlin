package com.acme.transport.ops.emissionspublisher

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Emissions publisher publishers emissions's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("EmissionsPublisher")
    var failed = 0
    try {
        try {
            mirrorRiskRegistersToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-risk-registers-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryAuditEventRulesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-audit-event-rules-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffTelemetryReadingId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-telemetry-reading-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportFeatureFlagsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-feature-flags-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncPrescriptionId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-prescription-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushCarePlanMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-care-plan-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyVehicleGroupStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-vehicle-group-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffPipelineMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-pipeline-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryServiceScheduleStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-service-schedule-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleBroadcastsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-broadcasts-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayFuelTransactionReportsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-fuel-transaction-reports-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushYardId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-yard-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushYardName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-yard-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateTripLogTotalsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-trip-log-totals-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            routePrescriptionsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-prescriptions-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillServiceScheduleWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-service-schedule-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardTelemetryReadingsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-telemetry-readings-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportDocumentsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-documents-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncHouseholdTotalsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-household-totals-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleFuelCardReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-fuel-card-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeYardWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-yard-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardPayRunNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-pay-run-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayYardsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-yards-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
