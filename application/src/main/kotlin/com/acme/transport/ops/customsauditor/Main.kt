package com.acme.transport.ops.customsauditor

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Customs auditor auditors customs's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("CustomsAuditor")
    var failed = 0
    try {
        try {
            relayDeliveryExceptionOptionsCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-delivery-exception-options-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportSecretsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-secrets-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            replaySeatMapMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-seat-map-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportDriverHomeTotalsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-driver-home-totals-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDockRatesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-dock-rates-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateFuelTransactionCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-fuel-transaction-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDriverHomeReportTimezone(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-driver-home-report-timezone").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportDocksTimezone(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-docks-timezone").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncGeoFencesDescription(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-geo-fences-description").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleMetricSeriesRulesName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-metric-series-rules-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportTripLogNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-trip-log-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeSponsorsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-sponsors-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryCertificateMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-certificate-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyAuditEventEligibilityId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-audit-event-eligibility-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffFuelPolicyConflictStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-fuel-policy-conflict-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillReturnRequestStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-return-request-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryJobOpeningMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-job-opening-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorStockItemId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-stock-item-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayProofOfDeliveryWorkflowsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-proof-of-delivery-workflows-token").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
