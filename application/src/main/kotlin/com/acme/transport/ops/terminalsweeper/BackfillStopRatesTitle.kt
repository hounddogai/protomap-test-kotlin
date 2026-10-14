package com.acme.transport.ops.terminalsweeper

import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.compareCustomerSegmentOptionsRequest
import com.acme.customers.v1.customerSegment
import com.acme.recruiting.v1.JobOpeningServiceGrpcKt
import com.acme.recruiting.v1.cloneJobOpeningRequest
import com.acme.recruiting.v1.jobOpening
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.getStopRatesRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.routing.v1.StopCalculationService/GetStopRates and hands what it keeps to the sink of its kind. */
suspend fun backfillStopRatesTitle(channel: ManagedChannel) {
    val origin = StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub(channel)
    val got = origin.getStopRates(getStopRatesRequest { this.stopId = "primary-backfill-stop-rates-title" })
    val entry = BackfillStopRatesTitleRecord(
        title = got.stop.title,
        stopId = normalize(got.stop.stopId),
    )
    deliverBackfillStopRatesTitle("job-opening", entry, channel)
}

/** Where backfillStopRatesTitle hands each record. */
interface BackfillStopRatesTitleWriter {
    suspend fun write(rec: BackfillStopRatesTitleRecord)
}

/** What backfillStopRatesTitle keeps of each GetStopRates response. */
data class BackfillStopRatesTitleRecord(val title: String, val stopId: String)

/** Sends records to acme.recruiting.v1.JobOpeningService. */
class BackfillStopRatesTitleJobOpening(private val stub: JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub) : BackfillStopRatesTitleWriter {
    override suspend fun write(rec: BackfillStopRatesTitleRecord) {
        val request = cloneJobOpeningRequest {
            this.jobOpening = jobOpening { this.notes = rec.stopId }
            this.requestId = rec.title
        }
        stub.cloneJobOpening(request)
    }
}

/** Sends records to acme.customers.v1.CustomerSegmentCalculationService. */
class BackfillStopRatesTitleCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : BackfillStopRatesTitleWriter {
    override suspend fun write(rec: BackfillStopRatesTitleRecord) {
        val request = compareCustomerSegmentOptionsRequest {
            this.customerSegment = customerSegment { this.description = rec.title }
            this.reason = rec.stopId
        }
        stub.compareCustomerSegmentOptions(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class BackfillStopRatesTitleLogOnly : BackfillStopRatesTitleWriter {
    override suspend fun write(rec: BackfillStopRatesTitleRecord) {
        LoggerFactory.getLogger("BackfillStopRatesTitleRecord").atInfo().addKeyValue("title", rec.title).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverBackfillStopRatesTitle(kind: String, rec: BackfillStopRatesTitleRecord, channel: ManagedChannel) {
    var sink: BackfillStopRatesTitleWriter = BackfillStopRatesTitleLogOnly()
    when (kind) {
        "job-opening" -> sink = BackfillStopRatesTitleJobOpening(JobOpeningServiceGrpcKt.JobOpeningServiceCoroutineStub(channel))
        "customer-segment-calculation" -> sink = BackfillStopRatesTitleCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
