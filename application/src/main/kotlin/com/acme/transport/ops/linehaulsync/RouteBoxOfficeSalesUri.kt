package com.acme.transport.ops.linehaulsync

import com.acme.boxoffice.v1.BoxOfficeSaleQueryServiceGrpcKt
import com.acme.boxoffice.v1.summarizeBoxOfficeSalesRequest
import com.acme.catalog.v1.CountProductsRequestKt
import com.acme.catalog.v1.ProductQueryServiceGrpcKt
import com.acme.catalog.v1.countProductsRequest
import com.acme.driverapp.v1.DriverInboxApprovalServiceGrpcKt
import com.acme.driverapp.v1.getDriverInboxApprovalStatusRequest
import com.acme.drivers.v1.CountDutyStatusTotalsRequestKt
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.countDutyStatusTotalsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.boxoffice.v1.BoxOfficeSaleQueryService/SummarizeBoxOfficeSales and hands what it keeps to the sink of its kind. */
suspend fun routeBoxOfficeSalesUri(channel: ManagedChannel) {
    val source = BoxOfficeSaleQueryServiceGrpcKt.BoxOfficeSaleQueryServiceCoroutineStub(channel)
    val reply = source.summarizeBoxOfficeSales(summarizeBoxOfficeSalesRequest { this.reason = "primary-route-box-office-sales-uri" })
    val record = RouteBoxOfficeSalesUriRecord(
        downloadUri = reply.downloadUri,
        timezone = normalize(reply.period.timezone),
        status = reply.result.status,
        reportId = reply.reportId,
    )
    val registry = mutableMapOf<String, RouteBoxOfficeSalesUriWriter>()
    for (registration in listOf(
        RouteBoxOfficeSalesUriRegistration("product-query", RouteBoxOfficeSalesUriProductQuery(ProductQueryServiceGrpcKt.ProductQueryServiceCoroutineStub(channel))),
        RouteBoxOfficeSalesUriRegistration("driver-inbox-approval", RouteBoxOfficeSalesUriDriverInboxApproval(DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub(channel))),
        RouteBoxOfficeSalesUriRegistration("duty-status-reporting", RouteBoxOfficeSalesUriDutyStatusReporting(DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["driver-inbox-approval"] ?: return
    sink.forward(record)
}

/** Where routeBoxOfficeSalesUri hands each record. */
interface RouteBoxOfficeSalesUriWriter {
    suspend fun forward(rec: RouteBoxOfficeSalesUriRecord)
}

/** What routeBoxOfficeSalesUri keeps of each SummarizeBoxOfficeSales response. */
data class RouteBoxOfficeSalesUriRecord(val downloadUri: String, val timezone: String, val status: String, val reportId: String)

/** Writes records to acme.catalog.v1.ProductQueryService. */
class RouteBoxOfficeSalesUriProductQuery(private val stub: ProductQueryServiceGrpcKt.ProductQueryServiceCoroutineStub) : RouteBoxOfficeSalesUriWriter {
    override suspend fun forward(rec: RouteBoxOfficeSalesUriRecord) {
        val request = countProductsRequest {
            this.filter = CountProductsRequestKt.filter { this.ownerId = rec.reportId; this.region = rec.timezone }
            this.orderBy = rec.downloadUri
        }
        stub.countProducts(request)
    }
}

/** Sends records to acme.driverapp.v1.DriverInboxApprovalService. */
class RouteBoxOfficeSalesUriDriverInboxApproval(private val stub: DriverInboxApprovalServiceGrpcKt.DriverInboxApprovalServiceCoroutineStub) : RouteBoxOfficeSalesUriWriter {
    override suspend fun forward(rec: RouteBoxOfficeSalesUriRecord) {
        RpcBoundary.run("acme.driverapp.v1.DriverInboxApprovalService/GetDriverInboxApprovalStatus") { stub.getDriverInboxApprovalStatus(getDriverInboxApprovalStatusRequest { this.driverInboxId = rec.reportId }) }
    }
}

/** Writes records to acme.drivers.v1.DutyStatusReportingService. */
class RouteBoxOfficeSalesUriDutyStatusReporting(private val stub: DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub) : RouteBoxOfficeSalesUriWriter {
    override suspend fun forward(rec: RouteBoxOfficeSalesUriRecord) {
        val request = countDutyStatusTotalsRequest {
            this.filter = CountDutyStatusTotalsRequestKt.filter { this.query = rec.downloadUri; this.region = rec.reportId }
        }
        RpcBoundary.run("acme.drivers.v1.DutyStatusReportingService/CountDutyStatusTotals") { stub.countDutyStatusTotals(request) }
    }
}

/** A sink and the kind of record it takes. */
class RouteBoxOfficeSalesUriRegistration(val kind: String, val sink: RouteBoxOfficeSalesUriWriter)
