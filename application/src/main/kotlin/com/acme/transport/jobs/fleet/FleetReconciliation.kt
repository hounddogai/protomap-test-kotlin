package com.acme.transport.jobs.fleet

import com.acme.common.v1.actor
import com.acme.common.v1.pageRequest
import com.acme.common.v1.requestContext
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.getVehicleApprovalStatusRequest
import com.acme.fleet.v1.listPendingVehiclesRequest
import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/** Review every fleet page against the current system of record. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("FleetReconciliation")
    try {
        // The operator the job runs for acts in every call, as the servers downstream require.
        val context = requestContext { tenantId = tenant; this.actor = actor { userId = subject } }
        val listing = VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(channel)
        val lookup = VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(channel)
        var cursor = ""
        do {
            val batch = RpcBoundary.read {
                listing.listPendingVehicles(listPendingVehiclesRequest {
                    this.context = context
                    this.page = pageRequest { pageSize = 100; pageToken = cursor }
                })
            }
            for (record in batch.vehiclesList) {
                val request = getVehicleApprovalStatusRequest {
                    this.context = context
                    this.vehicleId = record.vehicleId
                }
                val current = RpcBoundary.read { lookup.getVehicleApprovalStatus(request) }
                log.atInfo().addKeyValue("status", current.result.status).log("Reviewed record")
            }
            val next = batch.pageInfo.nextPageToken
            check(next.isEmpty() || next != cursor) { "Listing repeated its page token" }
            cursor = next
        } while (cursor.isNotEmpty())
    } finally {
        channel.shutdownNow()
    }
}
