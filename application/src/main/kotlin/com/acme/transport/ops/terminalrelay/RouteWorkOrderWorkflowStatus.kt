package com.acme.transport.ops.terminalrelay

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.SuggestAppointmentsRequestKt
import com.acme.appointments.v1.suggestAppointmentsRequest
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.getTranslationKeyRatesRequest
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.signalWorkOrderWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.maintenance.v1.WorkOrderWorkflowService/SignalWorkOrderWorkflow and hands what it keeps to the sink of its kind. */
suspend fun routeWorkOrderWorkflowStatus(channel: ManagedChannel) {
    val upstream = WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel)
    val found = RpcBoundary.read { upstream.signalWorkOrderWorkflow(signalWorkOrderWorkflowRequest { this.workOrderId = "main-route-work-order-workflow-status" }) }
    val row = RouteWorkOrderWorkflowStatusRecord(
        status = found.outcome.status,
        referenceId = found.outcome.referenceId,
        message = found.result.message,
    )
    deliverRouteWorkOrderWorkflowStatus("appointment-query", row, channel)
}

/** Where routeWorkOrderWorkflowStatus hands each record. */
interface RouteWorkOrderWorkflowStatusDestination {
    suspend fun hand(rec: RouteWorkOrderWorkflowStatusRecord)
}

/** What routeWorkOrderWorkflowStatus keeps of each SignalWorkOrderWorkflow response. */
data class RouteWorkOrderWorkflowStatusRecord(val status: String, val referenceId: String, val message: String)

/** Writes records to acme.localization.v1.TranslationKeyCalculationService. */
class RouteWorkOrderWorkflowStatusTranslationKeyCalculation(private val stub: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub) : RouteWorkOrderWorkflowStatusDestination {
    override suspend fun hand(rec: RouteWorkOrderWorkflowStatusRecord) {
        RpcBoundary.run("acme.localization.v1.TranslationKeyCalculationService/GetTranslationKeyRates") { stub.getTranslationKeyRates(getTranslationKeyRatesRequest { this.translationKeyId = rec.status }) }
    }
}

/** Sends records to acme.appointments.v1.AppointmentQueryService. */
class RouteWorkOrderWorkflowStatusAppointmentQuery(private val stub: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub) : RouteWorkOrderWorkflowStatusDestination {
    override suspend fun hand(rec: RouteWorkOrderWorkflowStatusRecord) {
        val request = suggestAppointmentsRequest {
            this.orderBy = rec.status
            this.filter = SuggestAppointmentsRequestKt.filter { this.region = rec.referenceId }
        }
        RpcBoundary.run("acme.appointments.v1.AppointmentQueryService/SuggestAppointments") { stub.suggestAppointments(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteWorkOrderWorkflowStatusLogOnly : RouteWorkOrderWorkflowStatusDestination {
    override suspend fun hand(rec: RouteWorkOrderWorkflowStatusRecord) {
        LoggerFactory.getLogger("RouteWorkOrderWorkflowStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRouteWorkOrderWorkflowStatus(kind: String, rec: RouteWorkOrderWorkflowStatusRecord, channel: ManagedChannel) {
    var sink: RouteWorkOrderWorkflowStatusDestination = RouteWorkOrderWorkflowStatusLogOnly()
    when (kind) {
        "translation-key-calculation" -> sink = RouteWorkOrderWorkflowStatusTranslationKeyCalculation(TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel))
        "appointment-query" -> sink = RouteWorkOrderWorkflowStatusAppointmentQuery(AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
