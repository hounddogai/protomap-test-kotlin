package com.acme.transport.ops.crewingrelay

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Crewing relay relays crewing's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("CrewingRelay")
    var failed = 0
    try {
        try {
            settlePreferenceTotalsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-preference-totals-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateStockItemsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-stock-items-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleLineageNodeId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-lineage-node-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncTourId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-tour-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushLedgerAccountStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-ledger-account-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorVehicleGroupId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-vehicle-group-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyDockName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-dock-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushInspectionMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-inspection-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayConcessionStandsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-concession-stands-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorFuelCardImportColumnsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-fuel-card-import-columns-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillFeatureFlagEligibilityId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-feature-flag-eligibility-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyTelemetryReadingId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-telemetry-reading-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayTripLogExportStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-trip-log-export-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffDocumentsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-documents-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffToursId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-tours-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryTripOptionsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-trip-options-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayCertificateNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-certificate-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportOrderStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-order-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyDeliveryExceptionWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-delivery-exception-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffWorkOrderMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-work-order-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportYardMoveId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-yard-move-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayTripLogsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-trip-logs-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportCustomerSegmentApprovalName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-customer-segment-approval-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncDocksId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-docks-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayCorporateEventStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-corporate-event-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayRouteBindingsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-route-bindings-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleRoutePlanWorkflowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-route-plan-workflow-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardCryptoKeyCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-crypto-key-code").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
