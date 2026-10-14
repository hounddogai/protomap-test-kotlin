package com.acme.transport.ops.networkmirror

import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.getFuelCardRatesRequest
import com.acme.fuel.v1.renewFuelCardRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelCardLifecycleService/RenewFuelCard and hands what it keeps to acme.fuel.v1.FuelCardCalculationService. */
suspend fun routeFuelCardStatus(channel: ManagedChannel) {
    val origin = FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(channel)
    val current = origin.renewFuelCard(renewFuelCardRequest { this.fuelCardId = "nightly-route-fuel-card-status" })
    val rec = RouteFuelCardStatusRecord(
        status = current.outcome.status,
        fuelCardId = normalize(current.fuelCard.fuelCardId),
    )
    RouteFuelCardStatusFuelCardCalculation(FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub(channel)).forward(rec)
}

/** What routeFuelCardStatus keeps of each RenewFuelCard response. */
data class RouteFuelCardStatusRecord(val status: String, val fuelCardId: String)

/** Sends records to acme.fuel.v1.FuelCardCalculationService. */
class RouteFuelCardStatusFuelCardCalculation(private val stub: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub) {
    suspend fun forward(rec: RouteFuelCardStatusRecord) {
        stub.getFuelCardRates(getFuelCardRatesRequest { this.fuelCardId = rec.status })
    }
}
