package com.acme.transport.ops.linehaulimporter

import com.acme.customers.v1.HouseholdQueryServiceGrpcKt
import com.acme.customers.v1.countHouseholdsRequest
import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.RenewDriverLicenseRequestKt
import com.acme.drivers.v1.renewDriverLicenseRequest
import com.acme.metrics.v1.MetricSeriesQueryServiceGrpcKt
import com.acme.metrics.v1.SummarizeMetricSeriesesRequestKt
import com.acme.metrics.v1.summarizeMetricSeriesesRequest
import com.acme.orders.v1.OrderServiceGrpcKt
import com.acme.orders.v1.SearchOrdersRequestKt
import com.acme.orders.v1.searchOrdersRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.HouseholdQueryService/CountHouseholds and hands what it keeps to the sink of its kind. */
suspend fun relayHouseholdsMessage(channel: ManagedChannel) {
    val origin = HouseholdQueryServiceGrpcKt.HouseholdQueryServiceCoroutineStub(channel)
    val got = origin.countHouseholds(countHouseholdsRequest { this.orderBy = "primary-relay-households-message" })
    val row = RelayHouseholdsMessageRecord(
        message = got.result.message,
        status = got.result.status,
        correlationId = got.result.correlationId,
    )
    val sinks = mapOf<String, RelayHouseholdsMessageSink>(
        "driver-license-lifecycle" to RelayHouseholdsMessageDriverLicenseLifecycle(DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(channel)),
        "order" to RelayHouseholdsMessageOrder(OrderServiceGrpcKt.OrderServiceCoroutineStub(channel)),
        "metric-series-query" to RelayHouseholdsMessageMetricSeriesQuery(MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "driver-license-lifecycle") continue
        sinks[kind]?.write(row)
    }
}

/** Where relayHouseholdsMessage hands each record. */
interface RelayHouseholdsMessageSink {
    suspend fun write(rec: RelayHouseholdsMessageRecord)
}

/** What relayHouseholdsMessage keeps of each CountHouseholds response. */
data class RelayHouseholdsMessageRecord(val message: String, val status: String, val correlationId: String)

/** Hands records to acme.drivers.v1.DriverLicenseLifecycleService. */
class RelayHouseholdsMessageDriverLicenseLifecycle(private val stub: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub) : RelayHouseholdsMessageSink {
    override suspend fun write(rec: RelayHouseholdsMessageRecord) {
        val request = renewDriverLicenseRequest {
            this.reason = rec.correlationId
            this.details = RenewDriverLicenseRequestKt.details { this.reasonCode = rec.message }
        }
        stub.renewDriverLicense(request)
    }
}

/** Writes records to acme.orders.v1.OrderService. */
class RelayHouseholdsMessageOrder(private val stub: OrderServiceGrpcKt.OrderServiceCoroutineStub) : RelayHouseholdsMessageSink {
    override suspend fun write(rec: RelayHouseholdsMessageRecord) {
        val request = searchOrdersRequest {
            this.filter = SearchOrdersRequestKt.filter { this.ownerId = rec.status }
        }
        stub.searchOrders(request)
    }
}

/** Sends records to acme.metrics.v1.MetricSeriesQueryService. */
class RelayHouseholdsMessageMetricSeriesQuery(private val stub: MetricSeriesQueryServiceGrpcKt.MetricSeriesQueryServiceCoroutineStub) : RelayHouseholdsMessageSink {
    override suspend fun write(rec: RelayHouseholdsMessageRecord) {
        val request = summarizeMetricSeriesesRequest {
            this.reason = rec.message
            this.details = SummarizeMetricSeriesesRequestKt.details { this.cron = rec.status }
            this.metricSeriesId = rec.correlationId
        }
        RpcBoundary.run("acme.metrics.v1.MetricSeriesQueryService/SummarizeMetricSerieses") { stub.summarizeMetricSerieses(request) }
    }
}
