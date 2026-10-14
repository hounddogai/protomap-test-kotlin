package com.acme.transport.ops.lastmilebackfill

import com.acme.fleet.v1.VehicleGroupValidationServiceGrpcKt
import com.acme.fleet.v1.validateVehicleGroupRequest
import com.acme.fleet.v1.vehicleGroup
import com.acme.maintenance.v1.WorkOrderValidationServiceGrpcKt
import com.acme.maintenance.v1.getWorkOrderRulesRequest
import com.acme.telehealth.v1.CalculateVideoVisitRequestKt
import com.acme.telehealth.v1.VideoVisitCalculationServiceGrpcKt
import com.acme.telehealth.v1.calculateVideoVisitRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.WorkOrderValidationService/GetWorkOrderRules and hands what it keeps to the sink of its kind. */
suspend fun propagateWorkOrderRulesId(channel: ManagedChannel) {
    val origin = WorkOrderValidationServiceGrpcKt.WorkOrderValidationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.getWorkOrderRules(getWorkOrderRulesRequest { this.workOrderId = "main-propagate-work-order-rules-id" }) }
    val row = PropagateWorkOrderRulesIdRecord(
        workOrderId = reply.workOrder.workOrderId,
        message = reply.result.message,
    )
    val sinks = mapOf<String, PropagateWorkOrderRulesIdWriter>(
        "video-visit-calculation" to PropagateWorkOrderRulesIdVideoVisitCalculation(VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub(channel)),
        "vehicle-group-validation" to PropagateWorkOrderRulesIdVehicleGroupValidation(VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "video-visit-calculation") continue
        sinks[kind]?.write(row)
    }
}

/** Where propagateWorkOrderRulesId hands each record. */
interface PropagateWorkOrderRulesIdWriter {
    suspend fun write(rec: PropagateWorkOrderRulesIdRecord)
}

/** What propagateWorkOrderRulesId keeps of each GetWorkOrderRules response. */
data class PropagateWorkOrderRulesIdRecord(val workOrderId: String, val message: String)

/** Hands records to acme.telehealth.v1.VideoVisitCalculationService. */
class PropagateWorkOrderRulesIdVideoVisitCalculation(private val stub: VideoVisitCalculationServiceGrpcKt.VideoVisitCalculationServiceCoroutineStub) : PropagateWorkOrderRulesIdWriter {
    override suspend fun write(rec: PropagateWorkOrderRulesIdRecord) {
        val request = calculateVideoVisitRequest {
            this.details = CalculateVideoVisitRequestKt.details { this.currencyCode = rec.workOrderId }
        }
        RpcBoundary.run("acme.telehealth.v1.VideoVisitCalculationService/CalculateVideoVisit") { stub.calculateVideoVisit(request) }
    }
}

/** Sends records to acme.fleet.v1.VehicleGroupValidationService. */
class PropagateWorkOrderRulesIdVehicleGroupValidation(private val stub: VehicleGroupValidationServiceGrpcKt.VehicleGroupValidationServiceCoroutineStub) : PropagateWorkOrderRulesIdWriter {
    override suspend fun write(rec: PropagateWorkOrderRulesIdRecord) {
        val request = validateVehicleGroupRequest {
            this.vehicleGroup = vehicleGroup { this.parentId = rec.workOrderId }
        }
        stub.validateVehicleGroup(request)
    }
}
