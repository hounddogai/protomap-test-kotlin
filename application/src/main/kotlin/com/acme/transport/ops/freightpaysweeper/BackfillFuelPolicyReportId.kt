package com.acme.transport.ops.freightpaysweeper

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.appointment
import com.acme.appointments.v1.createAppointmentRequest
import com.acme.fleet.v1.DelegateVehicleGroupApprovalRequestKt
import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.delegateVehicleGroupApprovalRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.fuel.v1.FuelPolicyReportingServiceGrpcKt
import com.acme.fuel.v1.shareFuelPolicyReportRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.recalculateShowsRequest
import com.acme.shows.v1.show
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyReportingService/ShareFuelPolicyReport and hands what it keeps to the sink of its kind. */
suspend fun backfillFuelPolicyReportId(channel: ManagedChannel) {
    val upstream = FuelPolicyReportingServiceGrpcKt.FuelPolicyReportingServiceCoroutineStub(channel)
    val current = upstream.shareFuelPolicyReport(shareFuelPolicyReportRequest { this.fuelPolicyId = "default-backfill-fuel-policy-report-id" })
    val rec = BackfillFuelPolicyReportIdRecord(
        fuelPolicyId = normalize(current.fuelPolicy.fuelPolicyId),
        status = current.result.status,
        name = current.fuelPolicy.name,
        outcomeStatus = current.outcome.status,
    )
    val sinks = mapOf<String, BackfillFuelPolicyReportIdTarget>(
        "show-calculation" to BackfillFuelPolicyReportIdShowCalculation(ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel)),
        "appointment" to BackfillFuelPolicyReportIdAppointment(AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel)),
        "vehicle-group-approval" to BackfillFuelPolicyReportIdVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)),
    )
    routeBackfillFuelPolicyReportId(sinks, "appointment", rec)
}

/** Where backfillFuelPolicyReportId hands each record. */
interface BackfillFuelPolicyReportIdTarget {
    suspend fun forward(rec: BackfillFuelPolicyReportIdRecord)
}

/** What backfillFuelPolicyReportId keeps of each ShareFuelPolicyReport response. */
data class BackfillFuelPolicyReportIdRecord(val fuelPolicyId: String, val status: String, val name: String, val outcomeStatus: String)

/** Writes records to acme.shows.v1.ShowCalculationService. */
class BackfillFuelPolicyReportIdShowCalculation(private val stub: ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub) : BackfillFuelPolicyReportIdTarget {
    override suspend fun forward(rec: BackfillFuelPolicyReportIdRecord) {
        val request = recalculateShowsRequest {
            this.showId = rec.name
            this.show = show { this.title = rec.status; this.showId = rec.fuelPolicyId }
        }
        stub.recalculateShows(request)
    }
}

/** Sends records to acme.appointments.v1.AppointmentService. */
class BackfillFuelPolicyReportIdAppointment(private val stub: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub) : BackfillFuelPolicyReportIdTarget {
    override suspend fun forward(rec: BackfillFuelPolicyReportIdRecord) {
        val request = createAppointmentRequest {
            this.appointment = appointment { this.title = rec.status; this.appointmentId = rec.name }
            this.requestId = rec.fuelPolicyId
        }
        stub.createAppointment(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleGroupApprovalService. */
class BackfillFuelPolicyReportIdVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : BackfillFuelPolicyReportIdTarget {
    override suspend fun forward(rec: BackfillFuelPolicyReportIdRecord) {
        val request = delegateVehicleGroupApprovalRequest {
            this.details = DelegateVehicleGroupApprovalRequestKt.details { this.comment = rec.outcomeStatus }
            this.reason = rec.status
            this.vehicleGroup = vehicleGroup { this.name = rec.name }
        }
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/DelegateVehicleGroupApproval") { stub.delegateVehicleGroupApproval(request) }
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeBackfillFuelPolicyReportId(sinks: Map<String, BackfillFuelPolicyReportIdTarget>, kind: String, rec: BackfillFuelPolicyReportIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
