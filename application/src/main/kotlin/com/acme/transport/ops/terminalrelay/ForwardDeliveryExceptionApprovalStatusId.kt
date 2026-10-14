package com.acme.transport.ops.terminalrelay

import com.acme.boxoffice.v1.BoxOfficeSaleCalculationServiceGrpcKt
import com.acme.boxoffice.v1.calculateBoxOfficeSaleRequest
import com.acme.deliveries.v1.DeliveryExceptionApprovalServiceGrpcKt
import com.acme.deliveries.v1.DeliveryServiceGrpcKt
import com.acme.deliveries.v1.cloneDeliveryRequest
import com.acme.deliveries.v1.getDeliveryExceptionApprovalStatusRequest
import com.acme.routing.v1.QuoteStopRequestKt
import com.acme.routing.v1.StopCalculationServiceGrpcKt
import com.acme.routing.v1.quoteStopRequest
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.lookupSponsorRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.deliveries.v1.DeliveryExceptionApprovalService/GetDeliveryExceptionApprovalStatus and hands what it keeps to the sink of its kind. */
suspend fun forwardDeliveryExceptionApprovalStatusId(channel: ManagedChannel) {
    val reader = DeliveryExceptionApprovalServiceGrpcKt.DeliveryExceptionApprovalServiceCoroutineStub(channel)
    val got = reader.getDeliveryExceptionApprovalStatus(getDeliveryExceptionApprovalStatusRequest { this.deliveryExceptionId = "default-forward-delivery-exception-approval-status-id" })
    val rec = ForwardDeliveryExceptionApprovalStatusIdRecord(
        correlationId = got.result.correlationId,
        subject = got.deliveryException.subject,
    )
    val sinks = mapOf<String, ForwardDeliveryExceptionApprovalStatusIdTarget>(
        "box-office-sale-calculation" to ForwardDeliveryExceptionApprovalStatusIdBoxOfficeSaleCalculation(BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub(channel)),
        "delivery" to ForwardDeliveryExceptionApprovalStatusIdDelivery(DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub(channel)),
        "stop-calculation" to ForwardDeliveryExceptionApprovalStatusIdStopCalculation(StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub(channel)),
        "sponsor-query" to ForwardDeliveryExceptionApprovalStatusIdSponsorQuery(SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)),
    )
    routeForwardDeliveryExceptionApprovalStatusId(sinks, "box-office-sale-calculation", rec)
}

/** Where forwardDeliveryExceptionApprovalStatusId hands each record. */
interface ForwardDeliveryExceptionApprovalStatusIdTarget {
    suspend fun forward(rec: ForwardDeliveryExceptionApprovalStatusIdRecord)
}

/** What forwardDeliveryExceptionApprovalStatusId keeps of each GetDeliveryExceptionApprovalStatus response. */
data class ForwardDeliveryExceptionApprovalStatusIdRecord(val correlationId: String, val subject: String)

/** Passes records to acme.boxoffice.v1.BoxOfficeSaleCalculationService. */
class ForwardDeliveryExceptionApprovalStatusIdBoxOfficeSaleCalculation(private val stub: BoxOfficeSaleCalculationServiceGrpcKt.BoxOfficeSaleCalculationServiceCoroutineStub) : ForwardDeliveryExceptionApprovalStatusIdTarget {
    override suspend fun forward(rec: ForwardDeliveryExceptionApprovalStatusIdRecord) {
        RpcBoundary.run("acme.boxoffice.v1.BoxOfficeSaleCalculationService/CalculateBoxOfficeSale") { stub.calculateBoxOfficeSale(calculateBoxOfficeSaleRequest { this.reason = rec.correlationId }) }
    }
}

/** Hands records to acme.deliveries.v1.DeliveryService. */
class ForwardDeliveryExceptionApprovalStatusIdDelivery(private val stub: DeliveryServiceGrpcKt.DeliveryServiceCoroutineStub) : ForwardDeliveryExceptionApprovalStatusIdTarget {
    override suspend fun forward(rec: ForwardDeliveryExceptionApprovalStatusIdRecord) {
        stub.cloneDelivery(cloneDeliveryRequest { this.requestId = rec.subject })
    }
}

/** Sends records to acme.routing.v1.StopCalculationService. */
class ForwardDeliveryExceptionApprovalStatusIdStopCalculation(private val stub: StopCalculationServiceGrpcKt.StopCalculationServiceCoroutineStub) : ForwardDeliveryExceptionApprovalStatusIdTarget {
    override suspend fun forward(rec: ForwardDeliveryExceptionApprovalStatusIdRecord) {
        val request = quoteStopRequest {
            this.details = QuoteStopRequestKt.details { this.currencyCode = rec.subject }
        }
        stub.quoteStop(request)
    }
}

/** Hands records to acme.sponsorships.v1.SponsorQueryService. */
class ForwardDeliveryExceptionApprovalStatusIdSponsorQuery(private val stub: SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub) : ForwardDeliveryExceptionApprovalStatusIdTarget {
    override suspend fun forward(rec: ForwardDeliveryExceptionApprovalStatusIdRecord) {
        stub.lookupSponsor(lookupSponsorRequest { this.sponsorId = rec.subject })
    }
}

/** Hands a record to the sink registered for its kind. */
suspend fun routeForwardDeliveryExceptionApprovalStatusId(sinks: Map<String, ForwardDeliveryExceptionApprovalStatusIdTarget>, kind: String, rec: ForwardDeliveryExceptionApprovalStatusIdRecord) {
    val sink = sinks[kind] ?: error("No sink registered for $kind")
    sink.forward(rec)
}
