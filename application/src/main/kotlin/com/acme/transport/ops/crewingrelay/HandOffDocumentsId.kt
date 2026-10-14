package com.acme.transport.ops.crewingrelay

import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.findDocumentsRequest
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.findCoursesRequest
import com.acme.routing.v1.AdvanceStopWorkflowRequestKt
import com.acme.routing.v1.StopWorkflowServiceGrpcKt
import com.acme.routing.v1.advanceStopWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.documents.v1.DocumentQueryService/FindDocuments and hands what it keeps to the sink of its kind. */
suspend fun handOffDocumentsId(channel: ManagedChannel) {
    val upstream = DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel)
    val reply = upstream.findDocuments(findDocumentsRequest { this.orderBy = "current-hand-off-documents-id" })
    val entry = HandOffDocumentsIdRecord(
        correlationId = reply.result.correlationId,
        nextPageToken = reply.pageInfo.nextPageToken,
    )
    actOnHandOffDocumentsId("course-query", entry, channel)
}

/** Where handOffDocumentsId hands each record. */
interface HandOffDocumentsIdTarget {
    suspend fun transmit(rec: HandOffDocumentsIdRecord)
}

/** What handOffDocumentsId keeps of each FindDocuments response. */
data class HandOffDocumentsIdRecord(val correlationId: String, val nextPageToken: String)

/** Hands records to acme.learning.v1.CourseQueryService. */
class HandOffDocumentsIdCourseQuery(private val stub: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub) : HandOffDocumentsIdTarget {
    override suspend fun transmit(rec: HandOffDocumentsIdRecord) {
        stub.findCourses(findCoursesRequest { this.orderBy = rec.correlationId })
    }
}

/** Passes records to acme.routing.v1.StopWorkflowService. */
class HandOffDocumentsIdStopWorkflow(private val stub: StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub) : HandOffDocumentsIdTarget {
    override suspend fun transmit(rec: HandOffDocumentsIdRecord) {
        val request = advanceStopWorkflowRequest {
            this.details = AdvanceStopWorkflowRequestKt.details { this.step = rec.nextPageToken }
            this.stopId = rec.correlationId
        }
        RpcBoundary.run("acme.routing.v1.StopWorkflowService/AdvanceStopWorkflow") { stub.advanceStopWorkflow(request) }
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnHandOffDocumentsId(kind: String, rec: HandOffDocumentsIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "course-query" to { HandOffDocumentsIdCourseQuery(CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(channel)).transmit(rec) },
        "stop-workflow" to { HandOffDocumentsIdStopWorkflow(StopWorkflowServiceGrpcKt.StopWorkflowServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
