package com.acme.transport.ops.emissionspublisher

import com.acme.billing.v1.BillingAccountServiceGrpcKt
import com.acme.billing.v1.billingAccount
import com.acme.billing.v1.updateBillingAccountRequest
import com.acme.common.v1.pageRequest
import com.acme.documents.v1.CountDocumentsRequestKt
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.countDocumentsRequest
import com.acme.prescriptions.v1.PrescriptionQueryServiceGrpcKt
import com.acme.prescriptions.v1.resolvePrescriptionsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.venues.v1.FindVenuesRequestKt
import com.acme.venues.v1.VenueQueryServiceGrpcKt
import com.acme.venues.v1.findVenuesRequest
import io.grpc.ManagedChannel

/** Reads acme.prescriptions.v1.PrescriptionQueryService/ResolvePrescriptions and hands what it keeps to the sink of its kind. */
suspend fun routePrescriptionsId(channel: ManagedChannel) {
    val reader = PrescriptionQueryServiceGrpcKt.PrescriptionQueryServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.resolvePrescriptions(resolvePrescriptionsRequest { }) }
    val row = RoutePrescriptionsIdRecord(
        correlationId = current.result.correlationId,
        nextPageToken = current.pageInfo.nextPageToken,
        message = normalize(current.result.message),
        status = current.result.status,
    )
    val sinks = mapOf<String, RoutePrescriptionsIdTarget>(
        "billing-account" to RoutePrescriptionsIdBillingAccount(BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub(channel)),
        "document-query" to RoutePrescriptionsIdDocumentQuery(DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel)),
        "venue-query" to RoutePrescriptionsIdVenueQuery(VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub(channel)),
    )
    val sink = sinks[System.getenv("ROUTE_PRESCRIPTIONS_ID_TARGET")] ?: return
    sink.hand(row)
}

/** Where routePrescriptionsId hands each record. */
interface RoutePrescriptionsIdTarget {
    suspend fun hand(rec: RoutePrescriptionsIdRecord)
}

/** What routePrescriptionsId keeps of each ResolvePrescriptions response. */
data class RoutePrescriptionsIdRecord(val correlationId: String, val nextPageToken: String, val message: String, val status: String)

/** Writes records to acme.billing.v1.BillingAccountService. */
class RoutePrescriptionsIdBillingAccount(private val stub: BillingAccountServiceGrpcKt.BillingAccountServiceCoroutineStub) : RoutePrescriptionsIdTarget {
    override suspend fun hand(rec: RoutePrescriptionsIdRecord) {
        val request = updateBillingAccountRequest {
            this.billingAccount = billingAccount { this.billingAccountId = rec.status }
        }
        stub.updateBillingAccount(request)
    }
}

/** Hands records to acme.documents.v1.DocumentQueryService. */
class RoutePrescriptionsIdDocumentQuery(private val stub: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub) : RoutePrescriptionsIdTarget {
    override suspend fun hand(rec: RoutePrescriptionsIdRecord) {
        val request = countDocumentsRequest {
            this.filter = CountDocumentsRequestKt.filter { this.query = rec.nextPageToken; this.ownerId = rec.message }
        }
        stub.countDocuments(request)
    }
}

/** Hands records to acme.venues.v1.VenueQueryService. */
class RoutePrescriptionsIdVenueQuery(private val stub: VenueQueryServiceGrpcKt.VenueQueryServiceCoroutineStub) : RoutePrescriptionsIdTarget {
    override suspend fun hand(rec: RoutePrescriptionsIdRecord) {
        val request = findVenuesRequest {
            this.filter = FindVenuesRequestKt.filter { this.query = rec.status }
            this.page = pageRequest { this.pageToken = rec.message }
        }
        stub.findVenues(request)
    }
}
