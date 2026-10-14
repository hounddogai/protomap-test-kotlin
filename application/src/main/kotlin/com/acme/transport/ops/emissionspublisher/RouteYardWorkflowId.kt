package com.acme.transport.ops.emissionspublisher

import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.chatRoom
import com.acme.chat.v1.recalculateChatRoomsRequest
import com.acme.yards.v1.YardMoveReportingServiceGrpcKt
import com.acme.yards.v1.YardWorkflowServiceGrpcKt
import com.acme.yards.v1.pauseYardWorkflowRequest
import com.acme.yards.v1.scheduleYardMoveReportRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.YardWorkflowService/PauseYardWorkflow and hands what it keeps to the sink of its kind. */
suspend fun routeYardWorkflowId(channel: ManagedChannel) {
    val origin = YardWorkflowServiceGrpcKt.YardWorkflowServiceCoroutineStub(channel)
    val reply = origin.pauseYardWorkflow(pauseYardWorkflowRequest { this.reason = "nightly-route-yard-workflow-id" })
    val record = RouteYardWorkflowIdRecord(
        correlationId = reply.result.correlationId,
        yardId = reply.yard.yardId,
        name = reply.yard.name,
    )
    deliverRouteYardWorkflowId("chat-room-calculation", record, channel)
}

/** Where routeYardWorkflowId hands each record. */
interface RouteYardWorkflowIdSink {
    suspend fun hand(rec: RouteYardWorkflowIdRecord)
}

/** What routeYardWorkflowId keeps of each PauseYardWorkflow response. */
data class RouteYardWorkflowIdRecord(val correlationId: String, val yardId: String, val name: String)

/** Hands records to acme.chat.v1.ChatRoomCalculationService. */
class RouteYardWorkflowIdChatRoomCalculation(private val stub: ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub) : RouteYardWorkflowIdSink {
    override suspend fun hand(rec: RouteYardWorkflowIdRecord) {
        val request = recalculateChatRoomsRequest {
            this.reason = rec.name
            this.chatRoom = chatRoom { this.name = rec.correlationId }
        }
        stub.recalculateChatRooms(request)
    }
}

/** Passes records to acme.yards.v1.YardMoveReportingService. */
class RouteYardWorkflowIdYardMoveReporting(private val stub: YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub) : RouteYardWorkflowIdSink {
    override suspend fun hand(rec: RouteYardWorkflowIdRecord) {
        stub.scheduleYardMoveReport(scheduleYardMoveReportRequest { this.reason = rec.yardId })
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteYardWorkflowIdLogOnly : RouteYardWorkflowIdSink {
    override suspend fun hand(rec: RouteYardWorkflowIdRecord) {
        LoggerFactory.getLogger("RouteYardWorkflowIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRouteYardWorkflowId(kind: String, rec: RouteYardWorkflowIdRecord, channel: ManagedChannel) {
    var sink: RouteYardWorkflowIdSink = RouteYardWorkflowIdLogOnly()
    when (kind) {
        "chat-room-calculation" -> sink = RouteYardWorkflowIdChatRoomCalculation(ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub(channel))
        "yard-move-reporting" -> sink = RouteYardWorkflowIdYardMoveReporting(YardMoveReportingServiceGrpcKt.YardMoveReportingServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
