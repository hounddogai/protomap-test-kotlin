package com.acme.transport.ops.linehaulsync

import com.acme.broadcasts.v1.BroadcastServiceGrpcKt
import com.acme.broadcasts.v1.broadcast
import com.acme.broadcasts.v1.cloneBroadcastRequest
import com.acme.maintenance.v1.PartWorkflowServiceGrpcKt
import com.acme.maintenance.v1.SignalPartWorkflowRequestKt
import com.acme.maintenance.v1.signalPartWorkflowRequest
import com.acme.telematics.v1.GeoFenceLifecycleServiceGrpcKt
import com.acme.telematics.v1.expireGeoFenceRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.GeoFenceLifecycleService/ExpireGeoFence and hands what it keeps to the sink of its kind. */
suspend fun mirrorGeoFenceMessage(channel: ManagedChannel) {
    val upstream = GeoFenceLifecycleServiceGrpcKt.GeoFenceLifecycleServiceCoroutineStub(channel)
    val found = upstream.expireGeoFence(expireGeoFenceRequest { this.reason = "main-mirror-geo-fence-message" })
    val record = MirrorGeoFenceMessageRecord(
        message = found.outcome.message,
        correlationId = normalize(found.result.correlationId),
        name = found.geoFence.name,
        geoFenceId = found.geoFence.geoFenceId,
    )
    dispatchMirrorGeoFenceMessage("part-workflow", record, channel)
}

/** Where mirrorGeoFenceMessage hands each record. */
interface MirrorGeoFenceMessageDestination {
    suspend fun write(rec: MirrorGeoFenceMessageRecord)
}

/** What mirrorGeoFenceMessage keeps of each ExpireGeoFence response. */
data class MirrorGeoFenceMessageRecord(val message: String, val correlationId: String, val name: String, val geoFenceId: String)

/** Hands records to acme.maintenance.v1.PartWorkflowService. */
class MirrorGeoFenceMessagePartWorkflow(private val stub: PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub) : MirrorGeoFenceMessageDestination {
    override suspend fun write(rec: MirrorGeoFenceMessageRecord) {
        val request = signalPartWorkflowRequest {
            this.details = SignalPartWorkflowRequestKt.details { this.initiatedBy = rec.message }
            this.partId = rec.name
        }
        stub.signalPartWorkflow(request)
    }
}

/** Writes records to acme.broadcasts.v1.BroadcastService. */
class MirrorGeoFenceMessageBroadcast(private val stub: BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub) : MirrorGeoFenceMessageDestination {
    override suspend fun write(rec: MirrorGeoFenceMessageRecord) {
        RpcBoundary.run("acme.broadcasts.v1.BroadcastService/CloneBroadcast") { stub.cloneBroadcast(cloneBroadcastRequest { this.broadcast = broadcast { this.title = rec.name } }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorGeoFenceMessageLogOnly : MirrorGeoFenceMessageDestination {
    override suspend fun write(rec: MirrorGeoFenceMessageRecord) {
        LoggerFactory.getLogger("MirrorGeoFenceMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchMirrorGeoFenceMessage(kind: String, rec: MirrorGeoFenceMessageRecord, channel: ManagedChannel) {
    var write: suspend (MirrorGeoFenceMessageRecord) -> Unit = MirrorGeoFenceMessageLogOnly()::write
    when (kind) {
        "part-workflow" -> write = MirrorGeoFenceMessagePartWorkflow(PartWorkflowServiceGrpcKt.PartWorkflowServiceCoroutineStub(channel))::write
        "broadcast" -> write = MirrorGeoFenceMessageBroadcast(BroadcastServiceGrpcKt.BroadcastServiceCoroutineStub(channel))::write
    }
    write(rec)
}
