package com.acme.transport.ops.safetyexporter

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Safety exporter exporters safety's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("SafetyExporter")
    var failed = 0
    try {
        try {
            pushRoutePlanNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-route-plan-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeDispatchOrderMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-dispatch-order-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillRouteRulesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-route-rules-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncStopWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-stop-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardVenueRulesMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-venue-rules-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayVehicleGroupsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-vehicle-groups-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardProofOfDeliveryWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-proof-of-delivery-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateFuelPolicyId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-fuel-policy-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeCustomerId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-customer-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffGetCustomersId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-get-customers-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorGetDriversMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-get-drivers-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncTelemetryReadingsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-telemetry-readings-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushAccessPolicyName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-access-policy-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportDispatchOrderWorkflowNumber(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-dispatch-order-workflow-number").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillFuelCardExportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-fuel-card-export-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorRouteId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-route-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleServiceScheduleWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-service-schedule-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardRateLimitsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-rate-limits-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushAdmissionsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-admissions-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardSecretStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-secret-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeOnboardingPlanId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-onboarding-plan-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryChatRoomOptionsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-chat-room-options-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffRoutePlanWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-route-plan-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayGeoFenceMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-geo-fence-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateRoutePlanWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-route-plan-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeCorporateEventMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-corporate-event-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
