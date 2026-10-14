package com.acme.transport.ops.linehaulsync

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.countAppointmentsRequest
import com.acme.common.v1.pageRequest
import com.acme.routing.v1.StopApprovalServiceGrpcKt
import com.acme.routing.v1.recallStopRequest
import com.acme.routing.v1.stop
import com.acme.telematics.v1.GeoFenceReportingServiceGrpcKt
import com.acme.telematics.v1.fetchGeoFenceReportRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceReportingService/FetchGeoFenceReport and hands what it keeps to the sink of its kind. */
suspend fun propagateGeoFenceReportId(channel: ManagedChannel) {
    val origin = GeoFenceReportingServiceGrpcKt.GeoFenceReportingServiceCoroutineStub(channel)
    val reply = origin.fetchGeoFenceReport(fetchGeoFenceReportRequest { this.geoFenceId = "primary-propagate-geo-fence-report-id" })
    val entry = PropagateGeoFenceReportIdRecord(
        geoFenceId = reply.geoFence.geoFenceId,
        correlationId = reply.result.correlationId,
        name = reply.geoFence.name,
        status = reply.result.status,
    )
    val sinks = mapOf<String, PropagateGeoFenceReportIdTarget>(
        "stop-approval" to PropagateGeoFenceReportIdStopApproval(StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub(channel)),
        "appointment-query" to PropagateGeoFenceReportIdAppointmentQuery(AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "appointment-query") continue
        sinks[kind]?.hand(entry)
    }
}

/** Where propagateGeoFenceReportId hands each record. */
interface PropagateGeoFenceReportIdTarget {
    suspend fun hand(rec: PropagateGeoFenceReportIdRecord)
}

/** What propagateGeoFenceReportId keeps of each FetchGeoFenceReport response. */
data class PropagateGeoFenceReportIdRecord(val geoFenceId: String, val correlationId: String, val name: String, val status: String)

/** Passes records to acme.routing.v1.StopApprovalService. */
class PropagateGeoFenceReportIdStopApproval(private val stub: StopApprovalServiceGrpcKt.StopApprovalServiceCoroutineStub) : PropagateGeoFenceReportIdTarget {
    override suspend fun hand(rec: PropagateGeoFenceReportIdRecord) {
        val request = recallStopRequest {
            this.stop = stop { this.title = rec.name; this.stopId = rec.correlationId }
        }
        stub.recallStop(request)
    }
}

/** Passes records to acme.appointments.v1.AppointmentQueryService. */
class PropagateGeoFenceReportIdAppointmentQuery(private val stub: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub) : PropagateGeoFenceReportIdTarget {
    override suspend fun hand(rec: PropagateGeoFenceReportIdRecord) {
        RpcBoundary.run("acme.appointments.v1.AppointmentQueryService/CountAppointments") { stub.countAppointments(countAppointmentsRequest { this.page = pageRequest { this.pageToken = rec.geoFenceId } }) }
    }
}
