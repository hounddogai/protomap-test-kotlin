package com.acme.transport.ops.freightpayreplicator

import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.calculateFuelCardRequest
import com.acme.fuel.v1.createFuelCardRequest
import com.acme.fuel.v1.fuelCard
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelCardService/CreateFuelCard and hands what it keeps to acme.fuel.v1.FuelCardCalculationService. */
suspend fun replayFuelCardStatus(channel: ManagedChannel) {
    val reader = FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.createFuelCard(createFuelCardRequest { this.requestId = "default-replay-fuel-card-status" }) }
    val record = ReplayFuelCardStatusRecord(
        status = got.result.status,
        currencyCode = got.fuelCard.currencyCode,
    )
    ReplayFuelCardStatusFuelCardCalculation(FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub(channel)).hand(record)
}

/** What replayFuelCardStatus keeps of each CreateFuelCard response. */
data class ReplayFuelCardStatusRecord(val status: String, val currencyCode: String)

/** Passes records to acme.fuel.v1.FuelCardCalculationService. */
class ReplayFuelCardStatusFuelCardCalculation(private val stub: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub) {
    suspend fun hand(rec: ReplayFuelCardStatusRecord) {
        val request = calculateFuelCardRequest {
            this.reason = rec.status
            this.fuelCard = fuelCard { this.fuelCardId = rec.currencyCode }
        }
        stub.calculateFuelCard(request)
    }
}
