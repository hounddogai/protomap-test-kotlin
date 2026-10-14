package com.acme.transport.ops.crewingreconciler

import com.acme.boxoffice.v1.BoxOfficeSaleCalculationServiceGrpcKt
import com.acme.boxoffice.v1.EstimateBoxOfficeSaleRequestKt
import com.acme.boxoffice.v1.estimateBoxOfficeSaleRequest
import com.acme.fleet.v1.VehicleGroupCalculationServiceGrpcKt
import com.acme.fleet.v1.quoteVehicleGroupRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockCalculationServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.quoteDockRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fleet.v1.VehicleGroupCalculationService/QuoteVehicleGroup and hands what it keeps to the sink of its kind. */
suspend fun pushVehicleGroupId(channel: ManagedChannel) {
    val reader = VehicleGroupCalculationServiceGrpcKt.VehicleGroupCalculationServiceCoroutineStub(channel)
    val found = RpcBoundary.read { reader.quoteVehicleGroup(quoteVehicleGroupRequest { this.vehicleGroupId = "primary-push-vehicle-group-id" }) }
    val record = PushVehicleGroupIdRecord(
        correlationId = found.result.correlationId,
        currencyCode = found.breakdown.currencyCode,
    )
    deliverPushVehicleGroupId("box-office-sale-calculation", record, channel)
}

/** Where pushVehicleGroupId hands each record. */
interface PushVehicleGroupIdDestination {
    suspend fun transmit(rec: PushVehicleGroupIdRecord)
}

/** What pushVehicleGroupId keeps of each QuoteVehicleGroup response. */
data class PushVehicleGroupIdRecord(val correlationId: String, val currencyCode: String)

/** Writes records to acme.yards.v1.DockCalculationService. */
class PushVehicleGroupIdDockCalculation(private val stub: DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub) : PushVehicleGroupIdDestination {
    override suspend fun transmit(rec: PushVehicleGroupIdRecord) {
        RpcBoundary.run("acme.yards.v1.DockCalculationService/QuoteDock") { stub.quoteDock(quoteDockRequest { this.dock = dock { this.dockId = rec.currencyCode } }) }
    }
}

/** Passes records to acme.boxoffice.v1.BoxOfficeSaleCalculationService. */
class PushVehicleGroupIdBoxOfficeSaleCalculation(private val stub: BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub) : PushVehicleGroupIdDestination {
    override suspend fun transmit(rec: PushVehicleGroupIdRecord) {
        val request = estimateBoxOfficeSaleRequest {
            this.reason = rec.correlationId
            this.details = EstimateBoxOfficeSaleRequestKt.details { this.currencyCode = rec.currencyCode }
        }
        stub.estimateBoxOfficeSale(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PushVehicleGroupIdLogOnly : PushVehicleGroupIdDestination {
    override suspend fun transmit(rec: PushVehicleGroupIdRecord) {
        LoggerFactory.getLogger("PushVehicleGroupIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPushVehicleGroupId(kind: String, rec: PushVehicleGroupIdRecord, channel: ManagedChannel) {
    var sink: PushVehicleGroupIdDestination = PushVehicleGroupIdLogOnly()
    when (kind) {
        "dock-calculation" -> sink = PushVehicleGroupIdDockCalculation(DockCalculationServiceGrpcKt.DockCalculationServiceCoroutineStub(channel))
        "box-office-sale-calculation" -> sink = PushVehicleGroupIdBoxOfficeSaleCalculation(BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
