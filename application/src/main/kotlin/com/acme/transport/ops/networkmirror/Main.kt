package com.acme.transport.ops.networkmirror

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Network mirror mirrors network's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("NetworkMirror")
    var failed = 0
    try {
        try {
            routeGetAppointmentsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-get-appointments-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayProductsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-products-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorVehicleWorkflowCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-vehicle-workflow-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayCorporateEventId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-corporate-event-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeTelemetryReadingMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-telemetry-reading-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillRouteWorkflowNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-route-workflow-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryPrescriptionOptionsCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-prescription-options-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleYardMoveSubject(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-yard-move-subject").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorPolicyMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-policy-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportCryptoKeysMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-crypto-keys-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeFuelCardStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-fuel-card-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyRouteNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-route-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateTelemetryReadingSource(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-telemetry-reading-source").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorGetOrdersStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-get-orders-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportPerformersId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-performers-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffYardMoveId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-yard-move-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncRouteStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-route-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorLedgerAccountId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-ledger-account-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayProfileCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-profile-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardBuildingsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-buildings-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffAuditEventsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-audit-events-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayDocumentEligibilityMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-document-eligibility-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayYardMoveMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-yard-move-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
