package com.acme.transport.ops.networkmirror

import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.fuelCard
import com.acme.fuel.v1.rejectFuelCardRequest
import com.acme.identity.v1.UserValidationServiceGrpcKt
import com.acme.identity.v1.ValidateUserRequestKt
import com.acme.identity.v1.user
import com.acme.identity.v1.validateUserRequest
import com.acme.routing.v1.RouteQueryServiceGrpcKt
import com.acme.routing.v1.peekRouteRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockWorkflowServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.resumeDockWorkflowRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.RouteQueryService/PeekRoute and hands what it keeps to the sink of its kind. */
suspend fun copyRouteNotes(channel: ManagedChannel) {
    val upstream = RouteQueryServiceGrpcKt.RouteQueryServiceCoroutineStub(channel)
    val reply = upstream.peekRoute(peekRouteRequest { this.routeId = "live-copy-route-notes" })
    val record = CopyRouteNotesRecord(
        notes = reply.route.notes,
        status = reply.result.status,
        routeId = normalize(reply.route.routeId),
    )
    deliverCopyRouteNotes("fuel-card-approval", record, channel)
}

/** Where copyRouteNotes hands each record. */
interface CopyRouteNotesWriter {
    suspend fun relay(rec: CopyRouteNotesRecord)
}

/** What copyRouteNotes keeps of each PeekRoute response. */
data class CopyRouteNotesRecord(val notes: String, val status: String, val routeId: String)

/** Writes records to acme.fuel.v1.FuelCardApprovalService. */
class CopyRouteNotesFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : CopyRouteNotesWriter {
    override suspend fun relay(rec: CopyRouteNotesRecord) {
        val request = rejectFuelCardRequest {
            this.fuelCard = fuelCard { this.fuelCardId = rec.notes }
            this.reason = rec.routeId
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardApprovalService/RejectFuelCard") { stub.rejectFuelCard(request) }
    }
}

/** Writes records to acme.identity.v1.UserValidationService. */
class CopyRouteNotesUserValidation(private val stub: UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub) : CopyRouteNotesWriter {
    override suspend fun relay(rec: CopyRouteNotesRecord) {
        val request = validateUserRequest {
            this.user = user { this.userId = rec.routeId }
            this.details = ValidateUserRequestKt.details { this.locale = rec.notes }
            this.reason = rec.status
        }
        stub.validateUser(request)
    }
}

/** Hands records to acme.yards.v1.DockWorkflowService. */
class CopyRouteNotesDockWorkflow(private val stub: DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub) : CopyRouteNotesWriter {
    override suspend fun relay(rec: CopyRouteNotesRecord) {
        stub.resumeDockWorkflow(resumeDockWorkflowRequest { this.dock = dock { this.code = rec.routeId } })
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyRouteNotesLogOnly : CopyRouteNotesWriter {
    override suspend fun relay(rec: CopyRouteNotesRecord) {
        LoggerFactory.getLogger("CopyRouteNotesRecord").atInfo().addKeyValue("notes", rec.notes).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyRouteNotes(kind: String, rec: CopyRouteNotesRecord, channel: ManagedChannel) {
    var sink: CopyRouteNotesWriter = CopyRouteNotesLogOnly()
    when (kind) {
        "fuel-card-approval" -> sink = CopyRouteNotesFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel))
        "user-validation" -> sink = CopyRouteNotesUserValidation(UserValidationServiceGrpcKt.UserValidationServiceCoroutineStub(channel))
        "dock-workflow" -> sink = CopyRouteNotesDockWorkflow(DockWorkflowServiceGrpcKt.DockWorkflowServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
