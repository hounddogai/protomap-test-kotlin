package com.acme.transport.ops.safetyexporter

import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.ListPendingCustomersRequestKt
import com.acme.customers.v1.listPendingCustomersRequest
import com.acme.dispatch.v1.DispatchAssignmentCalculationServiceGrpcKt
import com.acme.dispatch.v1.DispatchOrderApprovalServiceGrpcKt
import com.acme.dispatch.v1.approveDispatchOrderRequest
import com.acme.dispatch.v1.dispatchAssignment
import com.acme.dispatch.v1.simulateDispatchAssignmentRequest
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.SummarizeCoursesRequestKt
import com.acme.learning.v1.summarizeCoursesRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.dispatch.v1.DispatchOrderApprovalService/ApproveDispatchOrder and hands what it keeps to the sink of its kind. */
suspend fun routeDispatchOrderMessage(channel: ManagedChannel) {
    val source = DispatchOrderApprovalServiceGrpcKt.DispatchOrderApprovalServiceCoroutineStub(channel)
    val got = source.approveDispatchOrder(approveDispatchOrderRequest { this.dispatchOrderId = "main-route-dispatch-order-message" })
    val rec = RouteDispatchOrderMessageRecord(
        message = normalize(got.outcome.message),
        notes = got.dispatchOrder.notes,
        referenceId = got.outcome.referenceId,
        correlationId = normalize(got.result.correlationId),
    )
    deliverRouteDispatchOrderMessage("customer-approval", rec, channel)
}

/** Where routeDispatchOrderMessage hands each record. */
interface RouteDispatchOrderMessageDestination {
    suspend fun hand(rec: RouteDispatchOrderMessageRecord)
}

/** What routeDispatchOrderMessage keeps of each ApproveDispatchOrder response. */
data class RouteDispatchOrderMessageRecord(val message: String, val notes: String, val referenceId: String, val correlationId: String)

/** Hands records to acme.dispatch.v1.DispatchAssignmentCalculationService. */
class RouteDispatchOrderMessageDispatchAssignmentCalculation(private val stub: DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub) : RouteDispatchOrderMessageDestination {
    override suspend fun hand(rec: RouteDispatchOrderMessageRecord) {
        val request = simulateDispatchAssignmentRequest {
            this.dispatchAssignment = dispatchAssignment { this.subject = rec.notes }
        }
        stub.simulateDispatchAssignment(request)
    }
}

/** Hands records to acme.learning.v1.CourseQueryService. */
class RouteDispatchOrderMessageCourseQuery(private val stub: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub) : RouteDispatchOrderMessageDestination {
    override suspend fun hand(rec: RouteDispatchOrderMessageRecord) {
        val request = summarizeCoursesRequest {
            this.details = SummarizeCoursesRequestKt.details { this.cron = rec.referenceId }
        }
        stub.summarizeCourses(request)
    }
}

/** Writes records to acme.customers.v1.CustomerApprovalService. */
class RouteDispatchOrderMessageCustomerApproval(private val stub: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub) : RouteDispatchOrderMessageDestination {
    override suspend fun hand(rec: RouteDispatchOrderMessageRecord) {
        val request = listPendingCustomersRequest {
            this.filter = ListPendingCustomersRequestKt.filter { this.ownerId = rec.message; this.query = rec.correlationId }
        }
        stub.listPendingCustomers(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteDispatchOrderMessageLogOnly : RouteDispatchOrderMessageDestination {
    override suspend fun hand(rec: RouteDispatchOrderMessageRecord) {
        LoggerFactory.getLogger("RouteDispatchOrderMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRouteDispatchOrderMessage(kind: String, rec: RouteDispatchOrderMessageRecord, channel: ManagedChannel) {
    var sink: RouteDispatchOrderMessageDestination = RouteDispatchOrderMessageLogOnly()
    when (kind) {
        "dispatch-assignment-calculation" -> sink = RouteDispatchOrderMessageDispatchAssignmentCalculation(DispatchAssignmentCalculationServiceGrpcKt.DispatchAssignmentCalculationServiceCoroutineStub(channel))
        "course-query" -> sink = RouteDispatchOrderMessageCourseQuery(CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(channel))
        "customer-approval" -> sink = RouteDispatchOrderMessageCustomerApproval(CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
