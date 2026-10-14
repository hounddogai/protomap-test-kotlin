package com.acme.transport.ops.lastmilebackfill

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.getAppointmentRequest
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.signalStopWorkflowRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.getYardRatesRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.StopWorkflowService/SignalStopWorkflow and hands what it keeps to the sink of its kind. */
suspend fun routeStopWorkflowStatus(channel: ManagedChannel) {
    val origin = StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.signalStopWorkflow(signalStopWorkflowRequest { this.reason = "primary-route-stop-workflow-status" }) }
    val row = RouteStopWorkflowStatusRecord(
        status = got.outcome.status,
        title = normalize(got.stop.title),
    )
    dispatchRouteStopWorkflowStatus("appointment", row, channel)
}

/** Where routeStopWorkflowStatus hands each record. */
interface RouteStopWorkflowStatusWriter {
    suspend fun write(rec: RouteStopWorkflowStatusRecord)
}

/** What routeStopWorkflowStatus keeps of each SignalStopWorkflow response. */
data class RouteStopWorkflowStatusRecord(val status: String, val title: String)

/** Sends records to acme.appointments.v1.AppointmentService. */
class RouteStopWorkflowStatusAppointment(private val stub: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub) : RouteStopWorkflowStatusWriter {
    override suspend fun write(rec: RouteStopWorkflowStatusRecord) {
        stub.getAppointment(getAppointmentRequest { this.appointmentId = rec.title })
    }
}

/** Writes records to acme.yards.v1.YardCalculationService. */
class RouteStopWorkflowStatusYardCalculation(private val stub: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub) : RouteStopWorkflowStatusWriter {
    override suspend fun write(rec: RouteStopWorkflowStatusRecord) {
        RpcBoundary.run("acme.yards.v1.YardCalculationService/GetYardRates") { stub.getYardRates(getYardRatesRequest { this.yardId = rec.status }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteStopWorkflowStatusLogOnly : RouteStopWorkflowStatusWriter {
    override suspend fun write(rec: RouteStopWorkflowStatusRecord) {
        LoggerFactory.getLogger("RouteStopWorkflowStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchRouteStopWorkflowStatus(kind: String, rec: RouteStopWorkflowStatusRecord, channel: ManagedChannel) {
    var write: suspend (RouteStopWorkflowStatusRecord) -> Unit = RouteStopWorkflowStatusLogOnly()::write
    when (kind) {
        "appointment" -> write = RouteStopWorkflowStatusAppointment(AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel))::write
        "yard-calculation" -> write = RouteStopWorkflowStatusYardCalculation(YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
