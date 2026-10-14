package com.acme.transport.ops.terminalsweeper

import com.acme.careplans.v1.CarePlanCalculationServiceGrpcKt
import com.acme.careplans.v1.ExplainCarePlanCalculationRequestKt
import com.acme.careplans.v1.carePlan
import com.acme.careplans.v1.explainCarePlanCalculationRequest
import com.acme.fleet.v1.ExpireInspectionRequestKt
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.expireInspectionRequest
import com.acme.fleet.v1.getVehicleRequest
import com.acme.maintenance.v1.DelegateWorkOrderApprovalRequestKt
import com.acme.maintenance.v1.WorkOrderApprovalServiceGrpcKt
import com.acme.maintenance.v1.delegateWorkOrderApprovalRequest
import com.acme.maintenance.v1.workOrder
import com.acme.shows.v1.ShowQueryServiceGrpcKt
import com.acme.shows.v1.countShowsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.shows.v1.ShowQueryService/CountShows and hands what it keeps to the sink of its kind. */
suspend fun mirrorShowsId(channel: ManagedChannel) {
    val reader = ShowQueryServiceGrpcKt.ShowQueryServiceCoroutineStub(channel)
    val current = reader.countShows(countShowsRequest { this.orderBy = "main-mirror-shows-id" })
    val record = MirrorShowsIdRecord(
        correlationId = current.result.correlationId,
        status = normalize(current.result.status),
        message = normalize(current.result.message),
    )
    val sinks = listOf<MirrorShowsIdTarget>(
        MirrorShowsIdWorkOrderApproval(WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub(channel)),
        MirrorShowsIdVehicle(VehicleServiceGrpcKt.VehicleServiceCoroutineStub(channel)),
        MirrorShowsIdInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel)),
        MirrorShowsIdCarePlanCalculation(CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub(channel)),
    )
    sinks[3].deliver(record)
}

/** Where mirrorShowsId hands each record. */
interface MirrorShowsIdTarget {
    suspend fun deliver(rec: MirrorShowsIdRecord)
}

/** What mirrorShowsId keeps of each CountShows response. */
data class MirrorShowsIdRecord(val correlationId: String, val status: String, val message: String)

/** Hands records to acme.maintenance.v1.WorkOrderApprovalService. */
class MirrorShowsIdWorkOrderApproval(private val stub: WorkOrderApprovalServiceGrpcKt.WorkOrderApprovalServiceCoroutineStub) : MirrorShowsIdTarget {
    override suspend fun deliver(rec: MirrorShowsIdRecord) {
        val request = delegateWorkOrderApprovalRequest {
            this.details = DelegateWorkOrderApprovalRequestKt.details { this.comment = rec.correlationId }
            this.workOrder = workOrder { this.workOrderId = rec.status; this.subject = rec.message }
        }
        stub.delegateWorkOrderApproval(request)
    }
}

/** Hands records to acme.fleet.v1.VehicleService. */
class MirrorShowsIdVehicle(private val stub: VehicleServiceGrpcKt.VehicleServiceCoroutineStub) : MirrorShowsIdTarget {
    override suspend fun deliver(rec: MirrorShowsIdRecord) {
        stub.getVehicle(getVehicleRequest { this.vehicleId = rec.message })
    }
}

/** Writes records to acme.fleet.v1.InspectionLifecycleService. */
class MirrorShowsIdInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : MirrorShowsIdTarget {
    override suspend fun deliver(rec: MirrorShowsIdRecord) {
        val request = expireInspectionRequest {
            this.inspectionId = rec.message
            this.reason = rec.status
            this.details = ExpireInspectionRequestKt.details { this.reasonCode = rec.correlationId }
        }
        stub.expireInspection(request)
    }
}

/** Writes records to acme.careplans.v1.CarePlanCalculationService. */
class MirrorShowsIdCarePlanCalculation(private val stub: CarePlanCalculationServiceGrpcKt.CarePlanCalculationServiceCoroutineStub) : MirrorShowsIdTarget {
    override suspend fun deliver(rec: MirrorShowsIdRecord) {
        val request = explainCarePlanCalculationRequest {
            this.carePlan = carePlan { this.notes = rec.message }
            this.carePlanId = rec.status
            this.details = ExplainCarePlanCalculationRequestKt.details { this.locale = rec.correlationId }
        }
        RpcBoundary.run("acme.careplans.v1.CarePlanCalculationService/ExplainCarePlanCalculation") { stub.explainCarePlanCalculation(request) }
    }
}
