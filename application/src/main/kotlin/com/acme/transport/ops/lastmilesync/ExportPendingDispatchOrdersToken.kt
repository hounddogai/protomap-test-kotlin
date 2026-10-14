package com.acme.transport.ops.lastmilesync

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.appointment
import com.acme.appointments.v1.createAppointmentRequest
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.listPendingDispatchOrdersRequest
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.geoFence
import com.acme.telematics.v1.quoteGeoFenceRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveLifecycleServiceGrpcKt
import com.acme.yards.v1.suspendYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.dispatch.v1.DispatchOrderApprovalService/ListPendingDispatchOrders and hands what it keeps to the sink of its kind. */
suspend fun exportPendingDispatchOrdersToken(channel: ManagedChannel) {
    val source = DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { source.listPendingDispatchOrders(listPendingDispatchOrdersRequest { this.orderBy = "main-export-pending-dispatch-orders-token" }) }
    val row = ExportPendingDispatchOrdersTokenRecord(
        nextPageToken = reply.pageInfo.nextPageToken,
        status = reply.result.status,
        message = reply.result.message,
    )
    actOnExportPendingDispatchOrdersToken("appointment", row, channel)
}

/** Where exportPendingDispatchOrdersToken hands each record. */
interface ExportPendingDispatchOrdersTokenDestination {
    suspend fun hand(rec: ExportPendingDispatchOrdersTokenRecord)
}

/** What exportPendingDispatchOrdersToken keeps of each ListPendingDispatchOrders response. */
data class ExportPendingDispatchOrdersTokenRecord(val nextPageToken: String, val status: String, val message: String)

/** Passes records to acme.yards.v1.YardMoveLifecycleService. */
class ExportPendingDispatchOrdersTokenYardMoveLifecycle(private val stub: YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub) : ExportPendingDispatchOrdersTokenDestination {
    override suspend fun hand(rec: ExportPendingDispatchOrdersTokenRecord) {
        RpcBoundary.run("acme.yards.v1.YardMoveLifecycleService/SuspendYardMove") { stub.suspendYardMove(suspendYardMoveRequest { this.yardMoveId = rec.nextPageToken }) }
    }
}

/** Writes records to acme.telematics.v1.GeoFenceCalculationService. */
class ExportPendingDispatchOrdersTokenGeoFenceCalculation(private val stub: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub) : ExportPendingDispatchOrdersTokenDestination {
    override suspend fun hand(rec: ExportPendingDispatchOrdersTokenRecord) {
        stub.quoteGeoFence(quoteGeoFenceRequest { this.geoFence = geoFence { this.name = rec.nextPageToken } })
    }
}

/** Sends records to acme.appointments.v1.AppointmentService. */
class ExportPendingDispatchOrdersTokenAppointment(private val stub: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub) : ExportPendingDispatchOrdersTokenDestination {
    override suspend fun hand(rec: ExportPendingDispatchOrdersTokenRecord) {
        val request = createAppointmentRequest {
            this.appointment = appointment { this.appointmentId = rec.nextPageToken }
        }
        stub.createAppointment(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnExportPendingDispatchOrdersToken(kind: String, rec: ExportPendingDispatchOrdersTokenRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "yard-move-lifecycle" to { ExportPendingDispatchOrdersTokenYardMoveLifecycle(YardMoveLifecycleServiceGrpcKt.YardMoveLifecycleServiceCoroutineStub(channel)).hand(rec) },
        "geo-fence-calculation" to { ExportPendingDispatchOrdersTokenGeoFenceCalculation(GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(channel)).hand(rec) },
        "appointment" to { ExportPendingDispatchOrdersTokenAppointment(AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel)).hand(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
