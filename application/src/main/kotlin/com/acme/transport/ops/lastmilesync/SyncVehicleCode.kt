package com.acme.transport.ops.lastmilesync

import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.rejectVehicleRequest
import com.acme.telematics.v1.CancelTripLogImportRequestKt
import com.acme.telematics.v1.TripLogImportServiceGrpcKt
import com.acme.telematics.v1.cancelTripLogImportRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.ListYardMoveWorkflowsRequestKt
import com.acme.yards.v1.YardMoveWorkflowServiceGrpcKt
import com.acme.yards.v1.listYardMoveWorkflowsRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.VehicleApprovalService/RejectVehicle and hands what it keeps to the sink of its kind. */
suspend fun syncVehicleCode(channel: ManagedChannel) {
    val source = VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(channel)
    val current = source.rejectVehicle(rejectVehicleRequest { this.vehicleId = "main-sync-vehicle-code" })
    val entry = SyncVehicleCodeRecord(
        code = current.vehicle.code,
        referenceId = normalize(current.outcome.referenceId),
        vehicleId = current.vehicle.vehicleId,
    )
    deliverSyncVehicleCode("yard-move-workflow", entry, channel)
}

/** Where syncVehicleCode hands each record. */
interface SyncVehicleCodeTarget {
    suspend fun hand(rec: SyncVehicleCodeRecord)
}

/** What syncVehicleCode keeps of each RejectVehicle response. */
data class SyncVehicleCodeRecord(val code: String, val referenceId: String, val vehicleId: String)

/** Passes records to acme.telematics.v1.TripLogImportService. */
class SyncVehicleCodeTripLogImport(private val stub: TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub) : SyncVehicleCodeTarget {
    override suspend fun hand(rec: SyncVehicleCodeRecord) {
        val request = cancelTripLogImportRequest {
            this.details = CancelTripLogImportRequestKt.details { this.reasonCode = rec.referenceId }
            this.tripLogId = rec.code
        }
        stub.cancelTripLogImport(request)
    }
}

/** Passes records to acme.yards.v1.YardMoveWorkflowService. */
class SyncVehicleCodeYardMoveWorkflow(private val stub: YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub) : SyncVehicleCodeTarget {
    override suspend fun hand(rec: SyncVehicleCodeRecord) {
        val request = listYardMoveWorkflowsRequest {
            this.filter = ListYardMoveWorkflowsRequestKt.filter { this.query = rec.code }
        }
        RpcBoundary.run("acme.yards.v1.YardMoveWorkflowService/ListYardMoveWorkflows") { stub.listYardMoveWorkflows(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncVehicleCodeLogOnly : SyncVehicleCodeTarget {
    override suspend fun hand(rec: SyncVehicleCodeRecord) {
        LoggerFactory.getLogger("SyncVehicleCodeRecord").atInfo().addKeyValue("code", rec.code).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncVehicleCode(kind: String, rec: SyncVehicleCodeRecord, channel: ManagedChannel) {
    var sink: SyncVehicleCodeTarget = SyncVehicleCodeLogOnly()
    when (kind) {
        "trip-log-import" -> sink = SyncVehicleCodeTripLogImport(TripLogImportServiceGrpcKt.TripLogImportServiceCoroutineStub(channel))
        "yard-move-workflow" -> sink = SyncVehicleCodeYardMoveWorkflow(YardMoveWorkflowServiceGrpcKt.YardMoveWorkflowServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
