package com.acme.transport.ops.freightpaysweeper

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Freightpay sweeper sweepers freightpay's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("FreightpaySweeper")
    var failed = 0
    try {
        try {
            syncFuelPolicyDescription(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-fuel-policy-description").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillRouteStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-route-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncRiskRegisterId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-risk-register-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleVideoVisitId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-video-visit-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorGetOnboardingPlansToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-get-onboarding-plans-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleTelemetryReadingMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-telemetry-reading-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportExpenseReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-expense-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyVehicleId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-vehicle-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushFuelCardId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-fuel-card-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillFuelPolicyReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-fuel-policy-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayPatientRatesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-patient-rates-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncDutyStatusKind(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-duty-status-kind").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillTripLogReportMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-trip-log-report-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateDriverHomeUpdatesMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-driver-home-updates-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDockStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-dock-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffYardId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-yard-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateTourStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-tour-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayStopWorkflowsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-stop-workflows-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleCustomerSegmentImportColumnsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-customer-segment-import-columns-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushProductCalculationMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-product-calculation-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
