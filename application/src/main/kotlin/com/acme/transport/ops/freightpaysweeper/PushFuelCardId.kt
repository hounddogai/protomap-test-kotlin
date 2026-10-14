package com.acme.transport.ops.freightpaysweeper

import com.acme.dispatch.v1.DispatchOrderValidationServiceGrpcKt
import com.acme.dispatch.v1.checkDispatchOrderEligibilityRequest
import com.acme.dispatch.v1.dispatchOrder
import com.acme.fleet.v1.VehicleValidationServiceGrpcKt
import com.acme.fleet.v1.previewVehicleRequest
import com.acme.fleet.v1.vehicle
import com.acme.fuel.v1.FuelCardLifecycleServiceGrpcKt
import com.acme.fuel.v1.reinstateFuelCardRequest
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.route
import com.acme.routing.v1.simulateRouteRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelCardLifecycleService/ReinstateFuelCard and hands what it keeps to the sink of its kind. */
suspend fun pushFuelCardId(channel: ManagedChannel) {
    val upstream = FuelCardLifecycleServiceGrpcKt.FuelCardLifecycleServiceCoroutineStub(channel)
    val found = upstream.reinstateFuelCard(reinstateFuelCardRequest { this.reason = "primary-push-fuel-card-id" })
    val entry = PushFuelCardIdRecord(
        fuelCardId = normalize(found.fuelCard.fuelCardId),
        message = found.outcome.message,
    )
    deliverPushFuelCardId("route-calculation", entry, channel)
}

/** Where pushFuelCardId hands each record. */
interface PushFuelCardIdTarget {
    suspend fun deliver(rec: PushFuelCardIdRecord)
}

/** What pushFuelCardId keeps of each ReinstateFuelCard response. */
data class PushFuelCardIdRecord(val fuelCardId: String, val message: String)

/** Hands records to acme.fleet.v1.VehicleValidationService. */
class PushFuelCardIdVehicleValidation(private val stub: VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub) : PushFuelCardIdTarget {
    override suspend fun deliver(rec: PushFuelCardIdRecord) {
        stub.previewVehicle(previewVehicleRequest { this.vehicle = vehicle { this.code = rec.fuelCardId } })
    }
}

/** Writes records to acme.routing.v1.RouteCalculationService. */
class PushFuelCardIdRouteCalculation(private val stub: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub) : PushFuelCardIdTarget {
    override suspend fun deliver(rec: PushFuelCardIdRecord) {
        stub.simulateRoute(simulateRouteRequest { this.route = route { this.routeId = rec.message } })
    }
}

/** Sends records to acme.dispatch.v1.DispatchOrderValidationService. */
class PushFuelCardIdDispatchOrderValidation(private val stub: DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub) : PushFuelCardIdTarget {
    override suspend fun deliver(rec: PushFuelCardIdRecord) {
        val request = checkDispatchOrderEligibilityRequest {
            this.dispatchOrder = dispatchOrder { this.number = rec.message; this.dispatchOrderId = rec.fuelCardId }
        }
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderValidationService/CheckDispatchOrderEligibility") { stub.checkDispatchOrderEligibility(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PushFuelCardIdLogOnly : PushFuelCardIdTarget {
    override suspend fun deliver(rec: PushFuelCardIdRecord) {
        LoggerFactory.getLogger("PushFuelCardIdRecord").atInfo().addKeyValue("fuelCardId", rec.fuelCardId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPushFuelCardId(kind: String, rec: PushFuelCardIdRecord, channel: ManagedChannel) {
    var sink: PushFuelCardIdTarget = PushFuelCardIdLogOnly()
    when (kind) {
        "vehicle-validation" -> sink = PushFuelCardIdVehicleValidation(VehicleValidationServiceGrpcKt.VehicleValidationServiceCoroutineStub(channel))
        "route-calculation" -> sink = PushFuelCardIdRouteCalculation(RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub(channel))
        "dispatch-order-validation" -> sink = PushFuelCardIdDispatchOrderValidation(DispatchOrderValidationServiceGrpcKt.DispatchOrderValidationServiceCoroutineStub(channel))
    }
    sink.deliver(rec)
}
