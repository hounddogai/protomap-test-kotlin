package com.acme.transport.ops.freightpaysweeper

import com.acme.customers.v1.HouseholdCalculationServiceGrpcKt
import com.acme.customers.v1.explainHouseholdCalculationRequest
import com.acme.customers.v1.household
import com.acme.fuel.v1.FuelTransactionCalculationServiceGrpcKt
import com.acme.fuel.v1.SimulateFuelTransactionRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.simulateFuelTransactionRequest
import com.acme.telematics.v1.ListPendingTelemetryReadingsRequestKt
import com.acme.telematics.v1.TelemetryReadingApprovalServiceGrpcKt
import com.acme.telematics.v1.listPendingTelemetryReadingsRequest
import com.acme.tours.v1.TourServiceGrpcKt
import com.acme.tours.v1.updateTourRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.tours.v1.TourService/UpdateTour and hands what it keeps to the sink of its kind. */
suspend fun propagateTourStatus(channel: ManagedChannel) {
    val reader = TourServiceGrpcKt.TourServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.updateTour(updateTourRequest { }) }
    val rec = PropagateTourStatusRecord(
        status = reply.result.status,
        message = reply.result.message,
    )
    val sinks = listOf<PropagateTourStatusDestination>(
        PropagateTourStatusFuelTransactionCalculation(FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub(channel)),
        PropagateTourStatusHouseholdCalculation(HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub(channel)),
        PropagateTourStatusTelemetryReadingApproval(TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub(channel)),
    )
    sinks[2].forward(rec)
}

/** Where propagateTourStatus hands each record. */
interface PropagateTourStatusDestination {
    suspend fun forward(rec: PropagateTourStatusRecord)
}

/** What propagateTourStatus keeps of each UpdateTour response. */
data class PropagateTourStatusRecord(val status: String, val message: String)

/** Sends records to acme.fuel.v1.FuelTransactionCalculationService. */
class PropagateTourStatusFuelTransactionCalculation(private val stub: FuelTransactionCalculationServiceGrpcKt.FuelTransactionCalculationServiceCoroutineStub) : PropagateTourStatusDestination {
    override suspend fun forward(rec: PropagateTourStatusRecord) {
        val request = simulateFuelTransactionRequest {
            this.fuelTransaction = fuelTransaction { this.fuelTransactionId = rec.message }
            this.details = SimulateFuelTransactionRequestKt.details { this.currencyCode = rec.status }
        }
        stub.simulateFuelTransaction(request)
    }
}

/** Hands records to acme.customers.v1.HouseholdCalculationService. */
class PropagateTourStatusHouseholdCalculation(private val stub: HouseholdCalculationServiceGrpcKt.HouseholdCalculationServiceCoroutineStub) : PropagateTourStatusDestination {
    override suspend fun forward(rec: PropagateTourStatusRecord) {
        val request = explainHouseholdCalculationRequest {
            this.household = household { this.name = rec.message }
            this.householdId = rec.status
        }
        RpcBoundary.run("acme.customers.v1.HouseholdCalculationService/ExplainHouseholdCalculation") { stub.explainHouseholdCalculation(request) }
    }
}

/** Passes records to acme.telematics.v1.TelemetryReadingApprovalService. */
class PropagateTourStatusTelemetryReadingApproval(private val stub: TelemetryReadingApprovalServiceGrpcKt.TelemetryReadingApprovalServiceCoroutineStub) : PropagateTourStatusDestination {
    override suspend fun forward(rec: PropagateTourStatusRecord) {
        val request = listPendingTelemetryReadingsRequest {
            this.filter = ListPendingTelemetryReadingsRequestKt.filter { this.query = rec.status }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingApprovalService/ListPendingTelemetryReadings") { stub.listPendingTelemetryReadings(request) }
    }
}
