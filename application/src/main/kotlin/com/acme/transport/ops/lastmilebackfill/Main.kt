package com.acme.transport.ops.lastmilebackfill

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Lastmile backfill backfills lastmile's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("LastmileBackfill")
    var failed = 0
    try {
        try {
            copyDockId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-dock-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeAuditEventId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-audit-event-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagatePartEligibilityStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-part-eligibility-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            carrySoftwareLicenseId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-software-license-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateCarePlansStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-care-plans-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayFuelPolicyVersionMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-fuel-policy-version-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateDockMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-dock-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateWorkOrderRulesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-work-order-rules-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffHouseholdsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-households-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffFuelPolicyWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-fuel-policy-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorVehicleGroupId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-vehicle-group-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayUsersId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-users-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillPartWorkflowName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-part-workflow-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeStopWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-stop-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushZoneWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-zone-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeCustomerId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-customer-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayServiceScheduleId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-service-schedule-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportCertificateId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-certificate-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffDriverInboxWorkflowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-driver-inbox-workflow-message").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
