package com.acme.transport.ops.linehaulimporter

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Linehaul importer importers linehaul's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("LinehaulImporter")
    var failed = 0
    try {
        try {
            mirrorTelemetryReadingConflictsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-telemetry-reading-conflicts-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayHouseholdsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-households-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyNotificationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-notification-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            forwardHouseholdApprovalId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "forward-household-approval-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyFuelPolicyLifecycleDescription(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-fuel-policy-lifecycle-description").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayMediaAssetsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-media-assets-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillAdmissionCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-admission-code").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffPendingFuelTransactionsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-pending-fuel-transactions-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            carryGetSuppliersStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "carry-get-suppliers-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateConsentRecordWorkflowId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-consent-record-workflow-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorOnboardingPlansStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-onboarding-plans-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateFixedAssetRatesMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-fixed-asset-rates-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateTranslationKeyName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-translation-key-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            settlePendingWorkOrdersId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-pending-work-orders-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleGeoFenceWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-geo-fence-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeGeoFenceId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-geo-fence-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillLineageNodeStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-lineage-node-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            settleHouseholdsToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "settle-households-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            replayFixedAssetName(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-fixed-asset-name").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorDriverLicenseReportsStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-driver-license-reports-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillSurveysToken(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-surveys-token").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffServiceScheduleMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-service-schedule-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushDutyStatusId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-duty-status-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeRegisteredModelId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-registered-model-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeDatasetId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-dataset-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushConcessionStandStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-concession-stand-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            exportBudgetId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-budget-id").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
