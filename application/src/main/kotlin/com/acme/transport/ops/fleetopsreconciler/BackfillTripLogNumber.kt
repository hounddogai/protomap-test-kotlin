package com.acme.transport.ops.fleetopsreconciler

import com.acme.common.v1.pageRequest
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.peekManagedDeviceRequest
import com.acme.learning.v1.CourseQueryServiceGrpcKt
import com.acme.learning.v1.SuggestCoursesRequestKt
import com.acme.learning.v1.suggestCoursesRequest
import com.acme.telematics.v1.TripLogCalculationServiceGrpcKt
import com.acme.telematics.v1.simulateTripLogRequest
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.scoreYardRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.telematics.v1.TripLogCalculationService/SimulateTripLog and hands what it keeps to the sink of its kind. */
suspend fun backfillTripLogNumber(channel: ManagedChannel) {
    val origin = TripLogCalculationServiceGrpcKt.TripLogCalculationServiceCoroutineStub(channel)
    val found = origin.simulateTripLog(simulateTripLogRequest { this.tripLogId = "primary-backfill-trip-log-number" })
    val row = BackfillTripLogNumberRecord(
        number = found.tripLog.number,
        message = found.result.message,
    )
    deliverBackfillTripLogNumber("yard-validation", row, channel)
}

/** Where backfillTripLogNumber hands each record. */
interface BackfillTripLogNumberSink {
    suspend fun write(rec: BackfillTripLogNumberRecord)
}

/** What backfillTripLogNumber keeps of each SimulateTripLog response. */
data class BackfillTripLogNumberRecord(val number: String, val message: String)

/** Sends records to acme.yards.v1.YardValidationService. */
class BackfillTripLogNumberYardValidation(private val stub: YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub) : BackfillTripLogNumberSink {
    override suspend fun write(rec: BackfillTripLogNumberRecord) {
        val request = scoreYardRequest {
            this.yard = yard { this.code = rec.message }
            this.yardId = rec.number
        }
        stub.scoreYard(request)
    }
}

/** Hands records to acme.devices.v1.ManagedDeviceQueryService. */
class BackfillTripLogNumberManagedDeviceQuery(private val stub: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub) : BackfillTripLogNumberSink {
    override suspend fun write(rec: BackfillTripLogNumberRecord) {
        stub.peekManagedDevice(peekManagedDeviceRequest { this.managedDeviceId = rec.message })
    }
}

/** Hands records to acme.learning.v1.CourseQueryService. */
class BackfillTripLogNumberCourseQuery(private val stub: CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub) : BackfillTripLogNumberSink {
    override suspend fun write(rec: BackfillTripLogNumberRecord) {
        val request = suggestCoursesRequest {
            this.page = pageRequest { this.pageToken = rec.message }
            this.filter = SuggestCoursesRequestKt.filter { this.query = rec.number }
        }
        stub.suggestCourses(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillTripLogNumberLogOnly : BackfillTripLogNumberSink {
    override suspend fun write(rec: BackfillTripLogNumberRecord) {
        LoggerFactory.getLogger("BackfillTripLogNumberRecord").atInfo().addKeyValue("number", rec.number).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillTripLogNumber(kind: String, rec: BackfillTripLogNumberRecord, channel: ManagedChannel) {
    var sink: BackfillTripLogNumberSink = BackfillTripLogNumberLogOnly()
    when (kind) {
        "yard-validation" -> sink = BackfillTripLogNumberYardValidation(YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel))
        "managed-device-query" -> sink = BackfillTripLogNumberManagedDeviceQuery(ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(channel))
        "course-query" -> sink = BackfillTripLogNumberCourseQuery(CourseQueryServiceGrpcKt.CourseQueryServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
