package com.acme.transport.ops.crewingreconciler

import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** Crewing reconciler reconcilers crewing's records between the services that hold them. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("CrewingReconciler")
    var failed = 0
    try {
        try {
            exportDatasetsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "export-datasets-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            relayDatasetStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-dataset-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushFuelCardExpiryMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-fuel-card-expiry-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffCustomerSegmentId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-customer-segment-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyTicketsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-tickets-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            handOffDriverInboxReportNotes(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "hand-off-driver-inbox-report-notes").setCause(error).log("Job failed")
            failed++
        }
        try {
            copyYardCalculationId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "copy-yard-calculation-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            routeWorkOrderWorkflowStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "route-work-order-workflow-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            replaySponsorsId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "replay-sponsors-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushVehicleGroupId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-vehicle-group-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            propagateDeliveryStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "propagate-delivery-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillDriverMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-driver-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushDockExpiryId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-dock-expiry-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            pushDispatchAssignmentsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "push-dispatch-assignments-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            relaySponsorsMessage(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "relay-sponsors-message").setCause(error).log("Job failed")
            failed++
        }
        try {
            backfillGeoFencesId(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "backfill-geo-fences-id").setCause(error).log("Job failed")
            failed++
        }
        try {
            syncAudiencesStatus(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "sync-audiences-status").setCause(error).log("Job failed")
            failed++
        }
        try {
            mirrorProductOptionsCode(channel)
        } catch (error: StatusException) {
            log.atWarn().addKeyValue("job", "mirror-product-options-code").setCause(error).log("Job failed")
            failed++
        }
    } finally {
        channel.shutdownNow()
    }
    if (failed > 0) exitProcess(1)
}
