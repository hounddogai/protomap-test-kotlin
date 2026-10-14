package com.acme.transport.ops.linehaulsync

import com.acme.community.v1.ForumServiceGrpcKt
import com.acme.community.v1.cloneForumRequest
import com.acme.routing.v1.RoutePlanCalculationServiceGrpcKt
import com.acme.routing.v1.RouteWorkflowServiceGrpcKt
import com.acme.routing.v1.cancelRouteWorkflowRequest
import com.acme.routing.v1.estimateRoutePlanRequest
import com.acme.routing.v1.routePlan
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardCalculationServiceGrpcKt
import com.acme.yards.v1.calculateYardRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.RouteWorkflowService/CancelRouteWorkflow and hands what it keeps to the sink of its kind. */
suspend fun mirrorRouteWorkflowNotes(channel: ManagedChannel) {
    val reader = RouteWorkflowServiceGrpcKt.RouteWorkflowServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.cancelRouteWorkflow(cancelRouteWorkflowRequest { this.routeId = "nightly-mirror-route-workflow-notes" }) }
    val rec = MirrorRouteWorkflowNotesRecord(
        notes = got.route.notes,
        number = got.route.number,
        message = got.result.message,
        routeId = got.route.routeId,
    )
    dispatchMirrorRouteWorkflowNotes("route-plan-calculation", rec, channel)
}

/** Where mirrorRouteWorkflowNotes hands each record. */
interface MirrorRouteWorkflowNotesWriter {
    suspend fun write(rec: MirrorRouteWorkflowNotesRecord)
}

/** What mirrorRouteWorkflowNotes keeps of each CancelRouteWorkflow response. */
data class MirrorRouteWorkflowNotesRecord(val notes: String, val number: String, val message: String, val routeId: String)

/** Passes records to acme.community.v1.ForumService. */
class MirrorRouteWorkflowNotesForum(private val stub: ForumServiceGrpcKt.ForumServiceCoroutineStub) : MirrorRouteWorkflowNotesWriter {
    override suspend fun write(rec: MirrorRouteWorkflowNotesRecord) {
        stub.cloneForum(cloneForumRequest { this.requestId = rec.notes })
    }
}

/** Hands records to acme.routing.v1.RoutePlanCalculationService. */
class MirrorRouteWorkflowNotesRoutePlanCalculation(private val stub: RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub) : MirrorRouteWorkflowNotesWriter {
    override suspend fun write(rec: MirrorRouteWorkflowNotesRecord) {
        stub.estimateRoutePlan(estimateRoutePlanRequest { this.routePlan = routePlan { this.number = rec.number } })
    }
}

/** Passes records to acme.yards.v1.YardCalculationService. */
class MirrorRouteWorkflowNotesYardCalculation(private val stub: YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub) : MirrorRouteWorkflowNotesWriter {
    override suspend fun write(rec: MirrorRouteWorkflowNotesRecord) {
        stub.calculateYard(calculateYardRequest { this.reason = rec.number })
    }
}

/** Only logs, for kinds no service takes yet. */
class MirrorRouteWorkflowNotesLogOnly : MirrorRouteWorkflowNotesWriter {
    override suspend fun write(rec: MirrorRouteWorkflowNotesRecord) {
        LoggerFactory.getLogger("MirrorRouteWorkflowNotesRecord").atInfo().addKeyValue("notes", rec.notes).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchMirrorRouteWorkflowNotes(kind: String, rec: MirrorRouteWorkflowNotesRecord, channel: ManagedChannel) {
    var write: suspend (MirrorRouteWorkflowNotesRecord) -> Unit = MirrorRouteWorkflowNotesLogOnly()::write
    when (kind) {
        "forum" -> write = MirrorRouteWorkflowNotesForum(ForumServiceGrpcKt.ForumServiceCoroutineStub(channel))::write
        "route-plan-calculation" -> write = MirrorRouteWorkflowNotesRoutePlanCalculation(RoutePlanCalculationServiceGrpcKt.RoutePlanCalculationServiceCoroutineStub(channel))::write
        "yard-calculation" -> write = MirrorRouteWorkflowNotesYardCalculation(YardCalculationServiceGrpcKt.YardCalculationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
