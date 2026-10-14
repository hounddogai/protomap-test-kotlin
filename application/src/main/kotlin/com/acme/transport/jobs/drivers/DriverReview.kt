package com.acme.transport.jobs.drivers

import com.acme.common.v1.actor
import com.acme.common.v1.pageRequest
import com.acme.common.v1.requestContext
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.getDriverApprovalStatusRequest
import com.acme.drivers.v1.listPendingDriversRequest
import com.acme.transport.platform.CallerHeaders
import com.acme.transport.platform.RpcBoundary
import com.acme.transport.platform.Settings
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/** Review every drivers page against the current system of record. */
fun main() = runBlocking {
    val env = System.getenv()
    val tenant = requireNotNull(env["TENANT_ID"]) { "TENANT_ID is required" }
    val authorization = "Bearer " + requireNotNull(env["ACCESS_TOKEN"]) { "ACCESS_TOKEN is required" }
    val subject = requireNotNull(env["USER_ID"]) { "USER_ID is required" }
    val settings = Settings.fromEnvironment(env)
    val channel = ManagedChannelBuilder.forTarget(settings.meshTarget).usePlaintext()
        .intercept(CallerHeaders(authorization, tenant, subject)).build()
    val log = LoggerFactory.getLogger("DriverReview")
    try {
        // The operator the job runs for acts in every call, as the servers downstream require.
        val context = requestContext { tenantId = tenant; this.actor = actor { userId = subject } }
        val listing = DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel)
        val lookup = DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel)
        var cursor = ""
        do {
            val batch = RpcBoundary.read {
                listing.listPendingDrivers(listPendingDriversRequest {
                    this.context = context
                    this.page = pageRequest { pageSize = 100; pageToken = cursor }
                })
            }
            for (record in batch.driversList) {
                val request = getDriverApprovalStatusRequest {
                    this.context = context
                    this.driverId = record.driverId
                }
                val current = RpcBoundary.read { lookup.getDriverApprovalStatus(request) }
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
