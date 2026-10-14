package com.acme.transport.ops.terminalsweeper

import com.acme.common.v1.pageRequest
import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.batchGetDocumentsRequest
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.lookupDriverHomeRequest
import com.acme.fuel.v1.FuelCardWorkflowServiceGrpcKt
import com.acme.fuel.v1.ListFuelCardWorkflowsRequestKt
import com.acme.fuel.v1.listFuelCardWorkflowsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.RecallYardMoveRequestKt
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.recallYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.documents.v1.DocumentService/BatchGetDocuments and hands what it keeps to the sink of its kind. */
suspend fun syncGetDocumentsToken(channel: ManagedChannel) {
    val reader = DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel)
    val got = reader.batchGetDocuments(batchGetDocumentsRequest { })
    val rec = SyncGetDocumentsTokenRecord(
        nextPageToken = normalize(got.pageInfo.nextPageToken),
        message = got.result.message,
        correlationId = got.result.correlationId,
    )
    val registry = mutableMapOf<String, SyncGetDocumentsTokenWriter>()
    for (registration in listOf(
        SyncGetDocumentsTokenRegistration("driver-home-query", SyncGetDocumentsTokenDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel))),
        SyncGetDocumentsTokenRegistration("fuel-card-workflow", SyncGetDocumentsTokenFuelCardWorkflow(FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub(channel))),
        SyncGetDocumentsTokenRegistration("yard-move-approval", SyncGetDocumentsTokenYardMoveApproval(YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["driver-home-query"] ?: return
    sink.forward(rec)
}

/** Where syncGetDocumentsToken hands each record. */
interface SyncGetDocumentsTokenWriter {
    suspend fun forward(rec: SyncGetDocumentsTokenRecord)
}

/** What syncGetDocumentsToken keeps of each BatchGetDocuments response. */
data class SyncGetDocumentsTokenRecord(val nextPageToken: String, val message: String, val correlationId: String)

/** Hands records to acme.driverapp.v1.DriverHomeQueryService. */
class SyncGetDocumentsTokenDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : SyncGetDocumentsTokenWriter {
    override suspend fun forward(rec: SyncGetDocumentsTokenRecord) {
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/LookupDriverHome") { stub.lookupDriverHome(lookupDriverHomeRequest { this.driverHomeId = rec.correlationId }) }
    }
}

/** Writes records to acme.fuel.v1.FuelCardWorkflowService. */
class SyncGetDocumentsTokenFuelCardWorkflow(private val stub: FuelCardWorkflowServiceGrpcKt.FuelCardWorkflowServiceCoroutineStub) : SyncGetDocumentsTokenWriter {
    override suspend fun forward(rec: SyncGetDocumentsTokenRecord) {
        val request = listFuelCardWorkflowsRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
            this.filter = ListFuelCardWorkflowsRequestKt.filter { this.ownerId = rec.message }
        }
        stub.listFuelCardWorkflows(request)
    }
}

/** Passes records to acme.yards.v1.YardMoveApprovalService. */
class SyncGetDocumentsTokenYardMoveApproval(private val stub: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub) : SyncGetDocumentsTokenWriter {
    override suspend fun forward(rec: SyncGetDocumentsTokenRecord) {
        val request = recallYardMoveRequest {
            this.yardMoveId = rec.nextPageToken
            this.details = RecallYardMoveRequestKt.details { this.approverEmail = rec.correlationId }
        }
        stub.recallYardMove(request)
    }
}

/** A sink and the kind of record it takes. */
class SyncGetDocumentsTokenRegistration(val kind: String, val sink: SyncGetDocumentsTokenWriter)
