package com.acme.transport.ops.fleetopsbackfill

import com.acme.common.v1.pageRequest
import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.submitDriverRequest
import com.acme.lineage.v1.LineageNodeCalculationServiceGrpcKt
import com.acme.lineage.v1.RecalculateLineageNodesRequestKt
import com.acme.lineage.v1.lineageNode
import com.acme.lineage.v1.recalculateLineageNodesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.ListYardMoveReportsRequestKt
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardMoveValidationServiceGrpcKt
import com.acme.yards.v1.listYardMoveReportsRequest
import com.acme.yards.v1.normalizeYardMoveRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DriverApprovalService/SubmitDriver and hands what it keeps to the sink of its kind. */
suspend fun replayDriverMessage(channel: ManagedChannel) {
    val origin = DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel)
    val reply = origin.submitDriver(submitDriverRequest { this.driverId = "main-replay-driver-message" })
    val rec = ReplayDriverMessageRecord(
        message = reply.result.message,
        referenceId = reply.outcome.referenceId,
        outcomeMessage = reply.outcome.message,
        driverId = normalize(reply.driver.driverId),
    )
    val registry = mutableMapOf<String, ReplayDriverMessageDestination>()
    for (registration in listOf(
        ReplayDriverMessageRegistration("yard-move-reporting", ReplayDriverMessageYardMoveReporting(YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(channel))),
        ReplayDriverMessageRegistration("yard-move-validation", ReplayDriverMessageYardMoveValidation(YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub(channel))),
        ReplayDriverMessageRegistration("lineage-node-calculation", ReplayDriverMessageLineageNodeCalculation(LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["lineage-node-calculation"] ?: return
    sink.hand(rec)
}

/** Where replayDriverMessage hands each record. */
interface ReplayDriverMessageDestination {
    suspend fun hand(rec: ReplayDriverMessageRecord)
}

/** What replayDriverMessage keeps of each SubmitDriver response. */
data class ReplayDriverMessageRecord(val message: String, val referenceId: String, val outcomeMessage: String, val driverId: String)

/** Hands records to acme.yards.v1.YardMoveReportingService. */
class ReplayDriverMessageYardMoveReporting(private val stub: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub) : ReplayDriverMessageDestination {
    override suspend fun hand(rec: ReplayDriverMessageRecord) {
        val request = listYardMoveReportsRequest {
            this.filter = ListYardMoveReportsRequestKt.filter { this.ownerId = rec.referenceId; this.query = rec.message }
            this.page = pageRequest { this.pageToken = rec.driverId }
        }
        stub.listYardMoveReports(request)
    }
}

/** Passes records to acme.yards.v1.YardMoveValidationService. */
class ReplayDriverMessageYardMoveValidation(private val stub: YardMoveValidationServiceGrpcKt.YardMoveValidationServiceCoroutineStub) : ReplayDriverMessageDestination {
    override suspend fun hand(rec: ReplayDriverMessageRecord) {
        val request = normalizeYardMoveRequest {
            this.yardMove = yardMove { this.yardMoveId = rec.outcomeMessage; this.subject = rec.referenceId }
            this.yardMoveId = rec.driverId
        }
        RpcBoundary.run("acme.yards.v1.YardMoveValidationService/NormalizeYardMove") { stub.normalizeYardMove(request) }
    }
}

/** Sends records to acme.lineage.v1.LineageNodeCalculationService. */
class ReplayDriverMessageLineageNodeCalculation(private val stub: LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub) : ReplayDriverMessageDestination {
    override suspend fun hand(rec: ReplayDriverMessageRecord) {
        val request = recalculateLineageNodesRequest {
            this.lineageNode = lineageNode { this.lineageNodeId = rec.referenceId }
            this.details = RecalculateLineageNodesRequestKt.details { this.currencyCode = rec.message }
        }
        stub.recalculateLineageNodes(request)
    }
}

/** A sink and the kind of record it takes. */
class ReplayDriverMessageRegistration(val kind: String, val sink: ReplayDriverMessageDestination)
