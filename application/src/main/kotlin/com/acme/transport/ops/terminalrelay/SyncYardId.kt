package com.acme.transport.ops.terminalrelay

import com.acme.common.v1.pageRequest
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.ListJobOpeningsRequestKt
import com.acme.recruiting.v1.listJobOpeningsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.lookupYardRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardQueryService/LookupYard and hands what it keeps to acme.recruiting.v1.JobOpeningService. */
suspend fun syncYardId(channel: ManagedChannel) {
    val reader = YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.lookupYard(lookupYardRequest { this.yardId = "live-sync-yard-id" }) }
    val record = SyncYardIdRecord(
        yardId = normalize(found.yard.yardId),
        correlationId = normalize(found.result.correlationId),
        message = found.result.message,
    )
    SyncYardIdJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel)).deliver(record)
}

/** What syncYardId keeps of each LookupYard response. */
data class SyncYardIdRecord(val yardId: String, val correlationId: String, val message: String)

/** Sends records to acme.recruiting.v1.JobOpeningService. */
class SyncYardIdJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) {
    suspend fun deliver(rec: SyncYardIdRecord) {
        val request = listJobOpeningsRequest {
            this.page = pageRequest { this.pageToken = rec.message }
            this.filter = ListJobOpeningsRequestKt.filter { this.ownerId = rec.yardId; this.region = rec.correlationId }
        }
        RpcBoundary.run("acme.recruiting.v1.JobOpeningService/ListJobOpenings") { stub.listJobOpenings(request) }
    }
}
