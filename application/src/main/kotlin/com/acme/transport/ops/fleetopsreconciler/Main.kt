package com.acme.transport.ops.fleetopsreconciler

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Fleetops reconciler reconcilers fleetops's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("FleetopsReconciler")
    var failed = 0
    try {
        try {
            carryTelemetryReadingWorkflowKind(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-telemetry-reading-workflow-kind").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyPartReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-part-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardMetricSeriesesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-metric-serieses-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardDutyStatusMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-duty-status-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillTripLogNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-trip-log-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayPerformerMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-performer-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateStopWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-stop-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleCustomerSegmentImportsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-customer-segment-imports-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorCustomerSegmentMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-customer-segment-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillPrescriptionStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-prescription-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryFeatureFlagId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-feature-flag-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyCustomersStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-customers-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushCustomerSegmentsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-customer-segments-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateReturnRequestsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-return-requests-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushCryptoKeyCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-crypto-key-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayShowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-show-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeMetricSeriesCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-metric-series-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryOrdersMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-orders-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
