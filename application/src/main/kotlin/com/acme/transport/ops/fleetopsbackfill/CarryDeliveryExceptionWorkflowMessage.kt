package com.acme.transport.ops.fleetopsbackfill

import com.acme.configuration.v1.ConfigSetQueryServiceGrpcKt
import com.acme.configuration.v1.CountConfigSetsRequestKt
import com.acme.configuration.v1.countConfigSetsRequest
import com.acme.deliveries.v1.DeliveryExceptionWorkflowServiceGrpcKt
import com.acme.deliveries.v1.signalDeliveryExceptionWorkflowRequest
import com.acme.maintenance.v1.PartCalculationServiceGrpcKt
import com.acme.maintenance.v1.getPartRatesRequest
import com.acme.metrics.v1.BrowseMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.browseMetricSeriesesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryExceptionWorkflowService/SignalDeliveryExceptionWorkflow and hands what it keeps to the sink of its kind. */
suspend fun carryDeliveryExceptionWorkflowMessage(channel: ManagedChannel) {
    val upstream = DeliveryExceptionWorkflowServiceGrpcKt.DeliveryExceptionWorkflowServiceCoroutineStub(channel)
    val current = RpcBoundary.read { upstream.signalDeliveryExceptionWorkflow(signalDeliveryExceptionWorkflowRequest { this.deliveryExceptionId = "main-carry-delivery-exception-workflow-message" }) }
    val row = CarryDeliveryExceptionWorkflowMessageRecord(
        message = current.outcome.message,
        status = current.result.status,
    )
    val sinks = mapOf<String, CarryDeliveryExceptionWorkflowMessageTarget>(
        "config-set-query" to CarryDeliveryExceptionWorkflowMessageConfigSetQuery(ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub(channel)),
        "part-calculation" to CarryDeliveryExceptionWorkflowMessagePartCalculation(PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub(channel)),
        "metric-series-query" to CarryDeliveryExceptionWorkflowMessageMetricSeriesQuery(MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "part-calculation") continue
        sinks[kind]?.deliver(row)
    }
}

/** Where carryDeliveryExceptionWorkflowMessage hands each record. */
interface CarryDeliveryExceptionWorkflowMessageTarget {
    suspend fun deliver(rec: CarryDeliveryExceptionWorkflowMessageRecord)
}

/** What carryDeliveryExceptionWorkflowMessage keeps of each SignalDeliveryExceptionWorkflow response. */
data class CarryDeliveryExceptionWorkflowMessageRecord(val message: String, val status: String)

/** Hands records to acme.configuration.v1.ConfigSetQueryService. */
class CarryDeliveryExceptionWorkflowMessageConfigSetQuery(private val stub: ConfigSetQueryServiceGrpcKt.ConfigSetQueryServiceCoroutineStub) : CarryDeliveryExceptionWorkflowMessageTarget {
    override suspend fun deliver(rec: CarryDeliveryExceptionWorkflowMessageRecord) {
        val request = countConfigSetsRequest {
            this.filter = CountConfigSetsRequestKt.filter { this.query = rec.status }
            this.orderBy = rec.message
        }
        stub.countConfigSets(request)
    }
}

/** Hands records to acme.maintenance.v1.PartCalculationService. */
class CarryDeliveryExceptionWorkflowMessagePartCalculation(private val stub: PartCalculationServiceGrpcKt.PartCalculationServiceCoroutineStub) : CarryDeliveryExceptionWorkflowMessageTarget {
    override suspend fun deliver(rec: CarryDeliveryExceptionWorkflowMessageRecord) {
        stub.getPartRates(getPartRatesRequest { this.partId = rec.status })
    }
}

/** Passes records to acme.metrics.v1.MetricSeriesQueryService. */
class CarryDeliveryExceptionWorkflowMessageMetricSeriesQuery(private val stub: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub) : CarryDeliveryExceptionWorkflowMessageTarget {
    override suspend fun deliver(rec: CarryDeliveryExceptionWorkflowMessageRecord) {
        val request = browseMetricSeriesesRequest {
            this.orderBy = rec.status
            this.filter = BrowseMetricSeriesesRequestKt.filter { this.ownerId = rec.message }
        }
        stub.browseMetricSerieses(request)
    }
}
