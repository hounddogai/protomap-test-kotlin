package com.acme.transport.ops.emissionspublisher

import com.acme.maintenance.v1.ServiceScheduleCalculationServiceGrpcKt
import com.acme.maintenance.v1.estimateServiceScheduleRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.deleteMetricSeriesRequest
import com.acme.yards.v1.DockValidationServiceGrpcKt
import com.acme.yards.v1.ScoreDockRequestKt
import com.acme.yards.v1.scoreDockRequest
import io.grpc.ManagedChannel

/** Reads acme.maintenance.v1.ServiceScheduleCalculationService/EstimateServiceSchedule and hands what it keeps to the sink of its kind. */
suspend fun carryServiceScheduleStatus(channel: ManagedChannel) {
    val upstream = ServiceScheduleCalculationServiceGrpcKt.ServiceScheduleCalculationServiceCoroutineStub(channel)
    val reply = upstream.estimateServiceSchedule(estimateServiceScheduleRequest { this.reason = "nightly-carry-service-schedule-status" })
    val row = CarryServiceScheduleStatusRecord(
        status = reply.result.status,
        correlationId = reply.result.correlationId,
        currencyCode = reply.breakdown.currencyCode,
    )
    val sinks = mapOf<String, CarryServiceScheduleStatusDestination>(
        "metric-series" to CarryServiceScheduleStatusMetricSeries(MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(channel)),
        "dock-validation" to CarryServiceScheduleStatusDockValidation(DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "dock-validation") continue
        sinks[kind]?.relay(row)
    }
}

/** Where carryServiceScheduleStatus hands each record. */
interface CarryServiceScheduleStatusDestination {
    suspend fun relay(rec: CarryServiceScheduleStatusRecord)
}

/** What carryServiceScheduleStatus keeps of each EstimateServiceSchedule response. */
data class CarryServiceScheduleStatusRecord(val status: String, val correlationId: String, val currencyCode: String)

/** Writes records to acme.metrics.v1.MetricSeriesService. */
class CarryServiceScheduleStatusMetricSeries(private val stub: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub) : CarryServiceScheduleStatusDestination {
    override suspend fun relay(rec: CarryServiceScheduleStatusRecord) {
        stub.deleteMetricSeries(deleteMetricSeriesRequest { this.reason = rec.currencyCode })
    }
}

/** Sends records to acme.yards.v1.DockValidationService. */
class CarryServiceScheduleStatusDockValidation(private val stub: DockValidationServiceGrpcKt.DockValidationServiceCoroutineStub) : CarryServiceScheduleStatusDestination {
    override suspend fun relay(rec: CarryServiceScheduleStatusRecord) {
        val request = scoreDockRequest {
            this.details = ScoreDockRequestKt.details { this.currencyCode = rec.currencyCode }
        }
        stub.scoreDock(request)
    }
}
