package com.acme.transport.ops.crewingreconciler

import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.lintDispatchAssignmentsRequest
import com.acme.driverapp.v1.ConnectDriverInboxSourceRequestKt
import com.acme.driverapp.v1.DriverInboxIntegrationServiceGrpcKt
import com.acme.driverapp.v1.connectDriverInboxSourceRequest
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.driver
import com.acme.drivers.v1.updateDriverRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.SummarizeDocksRequestKt
import com.acme.yards.v1.summarizeDocksRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.dispatch.v1.DispatchAssignmentValidationService/LintDispatchAssignments and hands what it keeps to the sink of its kind. */
suspend fun pushDispatchAssignmentsMessage(channel: ManagedChannel) {
    val origin = DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel)
    val current = origin.lintDispatchAssignments(lintDispatchAssignmentsRequest { this.reason = "live-push-dispatch-assignments-message" })
    val rec = PushDispatchAssignmentsMessageRecord(
        message = current.outcome.message,
        subject = current.dispatchAssignment.subject,
        correlationId = current.result.correlationId,
        resultMessage = current.result.message,
    )
    deliverPushDispatchAssignmentsMessage("dock-query", rec, channel)
}

/** Where pushDispatchAssignmentsMessage hands each record. */
interface PushDispatchAssignmentsMessageTarget {
    suspend fun forward(rec: PushDispatchAssignmentsMessageRecord)
}

/** What pushDispatchAssignmentsMessage keeps of each LintDispatchAssignments response. */
data class PushDispatchAssignmentsMessageRecord(val message: String, val subject: String, val correlationId: String, val resultMessage: String)

/** Hands records to acme.drivers.v1.DriverService. */
class PushDispatchAssignmentsMessageDriver(private val stub: DriverServiceGrpcKt.DriverServiceCoroutineStub) : PushDispatchAssignmentsMessageTarget {
    override suspend fun forward(rec: PushDispatchAssignmentsMessageRecord) {
        stub.updateDriver(updateDriverRequest { this.driver = driver { this.driverId = rec.message } })
    }
}

/** Sends records to acme.yards.v1.DockQueryService. */
class PushDispatchAssignmentsMessageDockQuery(private val stub: DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub) : PushDispatchAssignmentsMessageTarget {
    override suspend fun forward(rec: PushDispatchAssignmentsMessageRecord) {
        val request = summarizeDocksRequest {
            this.details = SummarizeDocksRequestKt.details { this.cron = rec.subject }
        }
        stub.summarizeDocks(request)
    }
}

/** Hands records to acme.driverapp.v1.DriverInboxIntegrationService. */
class PushDispatchAssignmentsMessageDriverInboxIntegration(private val stub: DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub) : PushDispatchAssignmentsMessageTarget {
    override suspend fun forward(rec: PushDispatchAssignmentsMessageRecord) {
        val request = connectDriverInboxSourceRequest {
            this.details = ConnectDriverInboxSourceRequestKt.details { this.messageId = rec.correlationId; this.endpoint = rec.message }
            this.driverInboxId = rec.subject
        }
        RpcBoundary.run("acme.driverapp.v1.DriverInboxIntegrationService/ConnectDriverInboxSource") { stub.connectDriverInboxSource(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class PushDispatchAssignmentsMessageLogOnly : PushDispatchAssignmentsMessageTarget {
    override suspend fun forward(rec: PushDispatchAssignmentsMessageRecord) {
        LoggerFactory.getLogger("PushDispatchAssignmentsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPushDispatchAssignmentsMessage(kind: String, rec: PushDispatchAssignmentsMessageRecord, channel: ManagedChannel) {
    var sink: PushDispatchAssignmentsMessageTarget = PushDispatchAssignmentsMessageLogOnly()
    when (kind) {
        "driver" -> sink = PushDispatchAssignmentsMessageDriver(DriverServiceGrpcKt.DriverServiceCoroutineStub(channel))
        "dock-query" -> sink = PushDispatchAssignmentsMessageDockQuery(DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel))
        "driver-inbox-integration" -> sink = PushDispatchAssignmentsMessageDriverInboxIntegration(DriverInboxIntegrationServiceGrpcKt.DriverInboxIntegrationServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
