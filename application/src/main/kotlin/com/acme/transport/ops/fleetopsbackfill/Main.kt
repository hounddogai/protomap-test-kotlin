package com.acme.transport.ops.fleetopsbackfill

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Fleetops backfill backfills fleetops's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("FleetopsBackfill")
    var failed = 0
    try {
        try {
            mirrorTripLogStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-trip-log-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorContractMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-contract-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateFixedAssetCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-fixed-asset-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyDeliveryExceptionId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-delivery-exception-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayPoliciesCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-policies-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardLoanApplicationsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-loan-applications-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayComplianceCaseCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-compliance-case-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncMediaAssetCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-media-asset-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillPreferenceReportsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-preference-reports-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayDriverMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-driver-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            settlePolicyMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-policy-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryDeliveryExceptionWorkflowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-delivery-exception-workflow-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            settlePerformerMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-performer-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayFuelPolicyDecisionMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-fuel-policy-decision-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayYardWorkflowCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-yard-workflow-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffCashPositionCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-cash-position-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDriverHomesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-driver-homes-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardRoutePlanWorkflowMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-route-plan-workflow-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayPrescriptionCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-prescription-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportFixedAssetsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-fixed-assets-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateDutyStatusReportId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-duty-status-report-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleDriverLicenseApprovalStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-driver-license-approval-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayFuelPolicyExpiryId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-fuel-policy-expiry-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDeliveryWorkflowsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-delivery-workflows-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryGetAdmissionsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-get-admissions-token").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
