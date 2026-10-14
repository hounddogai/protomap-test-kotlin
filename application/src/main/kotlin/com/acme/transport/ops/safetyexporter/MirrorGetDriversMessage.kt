package com.acme.transport.ops.safetyexporter

import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.SimulateCustomerSegmentRequestKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.simulateCustomerSegmentRequest
import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.batchGetDriversRequest
import com.acme.surveys.v1.BrowseSurveysRequestKt
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.browseSurveysRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DriverService/BatchGetDrivers and hands what it keeps to the sink of its kind. */
suspend fun mirrorGetDriversMessage(channel: ManagedChannel) {
    val upstream = DriverServiceGrpcKt.DriverServiceCoroutineStub(channel)
    val reply = upstream.batchGetDrivers(batchGetDriversRequest { })
    val record = MirrorGetDriversMessageRecord(
        message = reply.result.message,
        status = normalize(reply.result.status),
        correlationId = normalize(reply.result.correlationId),
    )
    val sinks = listOf<MirrorGetDriversMessageSink>(
        MirrorGetDriversMessageSurveyQuery(SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(channel)),
        MirrorGetDriversMessageCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel)),
    )
    sinks[1].write(record)
}

/** Where mirrorGetDriversMessage hands each record. */
interface MirrorGetDriversMessageSink {
    suspend fun write(rec: MirrorGetDriversMessageRecord)
}

/** What mirrorGetDriversMessage keeps of each BatchGetDrivers response. */
data class MirrorGetDriversMessageRecord(val message: String, val status: String, val correlationId: String)

/** Hands records to acme.surveys.v1.SurveyQueryService. */
class MirrorGetDriversMessageSurveyQuery(private val stub: SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub) : MirrorGetDriversMessageSink {
    override suspend fun write(rec: MirrorGetDriversMessageRecord) {
        val request = browseSurveysRequest {
            this.orderBy = rec.correlationId
            this.filter = BrowseSurveysRequestKt.filter { this.region = rec.status }
        }
        stub.browseSurveys(request)
    }
}

/** Passes records to acme.customers.v1.CustomerSegmentCalculationService. */
class MirrorGetDriversMessageCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : MirrorGetDriversMessageSink {
    override suspend fun write(rec: MirrorGetDriversMessageRecord) {
        val request = simulateCustomerSegmentRequest {
            this.details = SimulateCustomerSegmentRequestKt.details { this.currencyCode = rec.correlationId }
            this.customerSegment = customerSegment { this.customerSegmentId = rec.status }
            this.customerSegmentId = rec.message
        }
        stub.simulateCustomerSegment(request)
    }
}
