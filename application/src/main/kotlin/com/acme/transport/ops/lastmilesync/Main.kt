package com.acme.transport.ops.lastmilesync

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Lastmile sync syncs lastmile's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("LastmileSync")
    var failed = 0
    try {
        try {
            syncDutyStatusApprovalId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-duty-status-approval-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryFanSessionsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-fan-sessions-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyShowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-show-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relaySeatMapId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-seat-map-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncNotificationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-notification-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncFuelPolicyMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-fuel-policy-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncTelemetryReadingSource(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-telemetry-reading-source").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportPendingDispatchOrdersToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-pending-dispatch-orders-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorZoneWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-zone-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayFuelTransactionWorkflowsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-fuel-transaction-workflows-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayCryptoKeysId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-crypto-keys-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportTripsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-trips-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardDocksId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-docks-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateSearchIndexMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-search-index-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncDeliveriesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-deliveries-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayTelemetryReadingsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-telemetry-readings-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncPerformerId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-performer-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayFuelPolicyImportName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-fuel-policy-import-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorServiceSchedulesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-service-schedules-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayTripLogMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-trip-log-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateReturnRequestNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-return-request-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardProofOfDeliveryId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-proof-of-delivery-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportWorkOrderApprovalSubject(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-work-order-approval-subject").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushVehicleCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-vehicle-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardInspectionSubject(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-inspection-subject").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateFixedAssetMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-fixed-asset-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncPipelineRatesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-pipeline-rates-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayDriverInboxWorkflowNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-driver-inbox-workflow-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncStockItemsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-stock-items-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncVehicleCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-vehicle-code").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
