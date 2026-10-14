package com.acme.transport.ops.lastmilebackfill

import com.acme.common.v1.pageRequest
import com.acme.identity.v1.UserQueryServiceGrpcKt
import com.acme.identity.v1.suggestUsersRequest
import com.acme.maintenance.v1.RenewWorkOrderRequestKt
import com.acme.maintenance.v1.WorkOrderLifecycleServiceGrpcKt
import com.acme.maintenance.v1.renewWorkOrderRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.normalizeDockRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.DockValidationService/NormalizeDock and hands what it keeps to the sink of its kind. */
suspend fun copyDockId(channel: ManagedChannel) {
    val origin = DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.normalizeDock(normalizeDockRequest { this.dockId = "primary-copy-dock-id" }) }
    val record = CopyDockIdRecord(
        correlationId = current.result.correlationId,
        referenceId = normalize(current.outcome.referenceId),
    )
    deliverCopyDockId("work-order-lifecycle", record, channel)
}

/** Where copyDockId hands each record. */
interface CopyDockIdSink {
    suspend fun relay(rec: CopyDockIdRecord)
}

/** What copyDockId keeps of each NormalizeDock response. */
data class CopyDockIdRecord(val correlationId: String, val referenceId: String)

/** Writes records to acme.identity.v1.UserQueryService. */
class CopyDockIdUserQuery(private val stub: UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub) : CopyDockIdSink {
    override suspend fun relay(rec: CopyDockIdRecord) {
        val request = suggestUsersRequest {
            this.page = pageRequest { this.pageToken = rec.correlationId }
            this.orderBy = rec.referenceId
        }
        stub.suggestUsers(request)
    }
}

/** Hands records to acme.maintenance.v1.WorkOrderLifecycleService. */
class CopyDockIdWorkOrderLifecycle(private val stub: WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub) : CopyDockIdSink {
    override suspend fun relay(rec: CopyDockIdRecord) {
        val request = renewWorkOrderRequest {
            this.workOrderId = rec.referenceId
            this.details = RenewWorkOrderRequestKt.details { this.reasonCode = rec.correlationId }
        }
        stub.renewWorkOrder(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyDockIdLogOnly : CopyDockIdSink {
    override suspend fun relay(rec: CopyDockIdRecord) {
        LoggerFactory.getLogger("CopyDockIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyDockId(kind: String, rec: CopyDockIdRecord, channel: ManagedChannel) {
    var sink: CopyDockIdSink = CopyDockIdLogOnly()
    when (kind) {
        "user-query" -> sink = CopyDockIdUserQuery(UserQueryServiceGrpcKt.UserQueryServiceCoroutineStub(channel))
        "work-order-lifecycle" -> sink = CopyDockIdWorkOrderLifecycle(WorkOrderLifecycleServiceGrpcKt.WorkOrderLifecycleServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
