package com.acme.transport.ops.fleetopsreconciler

import com.acme.customers.v1.CustomerApprovalServiceGrpcKt
import com.acme.customers.v1.ListPendingCustomersRequestKt
import com.acme.customers.v1.listPendingCustomersRequest
import com.acme.driverapp.v1.DriverHomeGatewayServiceGrpcKt
import com.acme.driverapp.v1.getDriverHomeSummaryRequest
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.updateMetricSeriesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveCalculationServiceGrpcKt
import com.acme.yards.v1.explainYardMoveCalculationRequest
import com.acme.yards.v1.yardMove
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.metrics.v1.MetricSeriesService/UpdateMetricSeries and hands what it keeps to the sink of its kind. */
suspend fun routeMetricSeriesCode(channel: ManagedChannel) {
    val upstream = MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(channel)
    val current = upstream.updateMetricSeries(updateMetricSeriesRequest { })
    val row = RouteMetricSeriesCodeRecord(
        code = normalize(current.metricSeries.code),
        metricSeriesId = current.metricSeries.metricSeriesId,
    )
    deliverRouteMetricSeriesCode("yard-move-calculation", row, channel)
}

/** Where routeMetricSeriesCode hands each record. */
interface RouteMetricSeriesCodeWriter {
    suspend fun forward(rec: RouteMetricSeriesCodeRecord)
}

/** What routeMetricSeriesCode keeps of each UpdateMetricSeries response. */
data class RouteMetricSeriesCodeRecord(val code: String, val metricSeriesId: String)

/** Passes records to acme.customers.v1.CustomerApprovalService. */
class RouteMetricSeriesCodeCustomerApproval(private val stub: CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub) : RouteMetricSeriesCodeWriter {
    override suspend fun forward(rec: RouteMetricSeriesCodeRecord) {
        val request = listPendingCustomersRequest {
            this.filter = ListPendingCustomersRequestKt.filter { this.query = rec.metricSeriesId }
        }
        stub.listPendingCustomers(request)
    }
}

/** Passes records to acme.yards.v1.YardMoveCalculationService. */
class RouteMetricSeriesCodeYardMoveCalculation(private val stub: YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub) : RouteMetricSeriesCodeWriter {
    override suspend fun forward(rec: RouteMetricSeriesCodeRecord) {
        val request = explainYardMoveCalculationRequest {
            this.yardMove = yardMove { this.subject = rec.metricSeriesId }
        }
        RpcBoundary.run("acme.yards.v1.YardMoveCalculationService/ExplainYardMoveCalculation") { stub.explainYardMoveCalculation(request) }
    }
}

/** Writes records to acme.driverapp.v1.DriverHomeGatewayService. */
class RouteMetricSeriesCodeDriverHomeGateway(private val stub: DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub) : RouteMetricSeriesCodeWriter {
    override suspend fun forward(rec: RouteMetricSeriesCodeRecord) {
        RpcBoundary.run("acme.driverapp.v1.DriverHomeGatewayService/GetDriverHomeSummary") { stub.getDriverHomeSummary(getDriverHomeSummaryRequest { this.driverHomeId = rec.metricSeriesId }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RouteMetricSeriesCodeLogOnly : RouteMetricSeriesCodeWriter {
    override suspend fun forward(rec: RouteMetricSeriesCodeRecord) {
        LoggerFactory.getLogger("RouteMetricSeriesCodeRecord").atInfo().addKeyValue("code", rec.code).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRouteMetricSeriesCode(kind: String, rec: RouteMetricSeriesCodeRecord, channel: ManagedChannel) {
    var sink: RouteMetricSeriesCodeWriter = RouteMetricSeriesCodeLogOnly()
    when (kind) {
        "customer-approval" -> sink = RouteMetricSeriesCodeCustomerApproval(CustomerApprovalServiceGrpcKt.CustomerApprovalServiceCoroutineStub(channel))
        "yard-move-calculation" -> sink = RouteMetricSeriesCodeYardMoveCalculation(YardMoveCalculationServiceGrpcKt.YardMoveCalculationServiceCoroutineStub(channel))
        "driver-home-gateway" -> sink = RouteMetricSeriesCodeDriverHomeGateway(DriverHomeGatewayServiceGrpcKt.DriverHomeGatewayServiceCoroutineStub(channel))
    }
    sink.forward(rec)
}
