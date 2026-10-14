package com.acme.transport.ops.emissionspublisher

import com.acme.audit.v1.AuditEventValidationServiceGrpcKt
import com.acme.audit.v1.getAuditEventRulesRequest
import com.acme.learning.v1.BrowseCoursesRequestKt
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.browseCoursesRequest
import com.acme.telematics.v1.ListTripLogExportsRequestKt
import com.acme.telematics.v1.TripLogExportServiceGrpcKt
import com.acme.telematics.v1.listTripLogExportsRequest
import com.acme.yards.v1.SimulateYardMoveRequestKt
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.simulateYardMoveRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.audit.v1.AuditEventValidationService/GetAuditEventRules and hands what it keeps to the sink of its kind. */
suspend fun carryAuditEventRulesId(channel: ManagedChannel) {
    val upstream = AuditEventValidationServiceGrpcKt.AuditEventValidationServiceCoroutineStub(channel)
    val current = upstream.getAuditEventRules(getAuditEventRulesRequest { this.auditEventId = "default-carry-audit-event-rules-id" })
    val entry = CarryAuditEventRulesIdRecord(
        correlationId = current.result.correlationId,
        source = current.auditEvent.source,
        auditEventCorrelationId = current.auditEvent.correlationId,
        message = current.result.message,
    )
    deliverCarryAuditEventRulesId("course-query", entry, channel)
}

/** Where carryAuditEventRulesId hands each record. */
interface CarryAuditEventRulesIdSink {
    suspend fun forward(rec: CarryAuditEventRulesIdRecord)
}

/** What carryAuditEventRulesId keeps of each GetAuditEventRules response. */
data class CarryAuditEventRulesIdRecord(val correlationId: String, val source: String, val auditEventCorrelationId: String, val message: String)

/** Hands records to acme.telematics.v1.TripLogExportService. */
class CarryAuditEventRulesIdTripLogExport(private val stub: TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub) : CarryAuditEventRulesIdSink {
    override suspend fun forward(rec: CarryAuditEventRulesIdRecord) {
        val request = listTripLogExportsRequest {
            this.filter = ListTripLogExportsRequestKt.filter { this.ownerId = rec.source }
            this.orderBy = rec.message
        }
        stub.listTripLogExports(request)
    }
}

/** Hands records to acme.learning.v1.CourseQueryService. */
class CarryAuditEventRulesIdCourseQuery(private val stub: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub) : CarryAuditEventRulesIdSink {
    override suspend fun forward(rec: CarryAuditEventRulesIdRecord) {
        val request = browseCoursesRequest {
            this.filter = BrowseCoursesRequestKt.filter { this.region = rec.source }
        }
        stub.browseCourses(request)
    }
}

/** Writes records to acme.yards.v1.YardMoveCalculationService. */
class CarryAuditEventRulesIdYardMoveCalculation(private val stub: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub) : CarryAuditEventRulesIdSink {
    override suspend fun forward(rec: CarryAuditEventRulesIdRecord) {
        val request = simulateYardMoveRequest {
            this.yardMoveId = rec.source
            this.details = SimulateYardMoveRequestKt.details { this.currencyCode = rec.correlationId }
        }
        stub.simulateYardMove(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class CarryAuditEventRulesIdLogOnly : CarryAuditEventRulesIdSink {
    override suspend fun forward(rec: CarryAuditEventRulesIdRecord) {
        LoggerFactory.getLogger("CarryAuditEventRulesIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCarryAuditEventRulesId(kind: String, rec: CarryAuditEventRulesIdRecord, channel: ManagedChannel) {
    var sink: CarryAuditEventRulesIdSink = CarryAuditEventRulesIdLogOnly()
    when (kind) {
        "trip-log-export" -> sink = CarryAuditEventRulesIdTripLogExport(TripLogExportServiceGrpcKt.TripLogExportServiceCoroutineStub(channel))
        "course-query" -> sink = CarryAuditEventRulesIdCourseQuery(CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(channel))
        "yard-move-calculation" -> sink = CarryAuditEventRulesIdYardMoveCalculation(YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
