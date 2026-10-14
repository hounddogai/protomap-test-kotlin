package com.acme.transport.ops.emissionspublisher

import com.acme.customers.v1.ConsentRecordValidationServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.getConsentRecordRulesRequest
import com.acme.customers.v1.recallCustomerSegmentRequest
import com.acme.fuel.v1.FuelTransactionReportingServiceGrpcKt
import com.acme.fuel.v1.listFuelTransactionReportsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelTransactionReportingService/ListFuelTransactionReports and hands what it keeps to the sink of its kind. */
suspend fun replayFuelTransactionReportsToken(channel: ManagedChannel) {
    val upstream = FuelTransactionReportingServiceGrpcKt.FuelTransactionReportingServiceCoroutineStub(channel)
    val got = upstream.listFuelTransactionReports(listFuelTransactionReportsRequest { this.orderBy = "main-replay-fuel-transaction-reports-token" })
    val rec = ReplayFuelTransactionReportsTokenRecord(
        nextPageToken = got.pageInfo.nextPageToken,
        correlationId = normalize(got.result.correlationId),
        message = got.result.message,
        status = got.result.status,
    )
    val registry = mutableMapOf<String, ReplayFuelTransactionReportsTokenDestination>()
    for (registration in listOf(
        ReplayFuelTransactionReportsTokenRegistration("consent-record-validation", ReplayFuelTransactionReportsTokenConsentRecordValidation(ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub(channel))),
        ReplayFuelTransactionReportsTokenRegistration("customer-segment-approval", ReplayFuelTransactionReportsTokenCustomerSegmentApproval(CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["consent-record-validation"] ?: return
    sink.transmit(rec)
}

/** Where replayFuelTransactionReportsToken hands each record. */
interface ReplayFuelTransactionReportsTokenDestination {
    suspend fun transmit(rec: ReplayFuelTransactionReportsTokenRecord)
}

/** What replayFuelTransactionReportsToken keeps of each ListFuelTransactionReports response. */
data class ReplayFuelTransactionReportsTokenRecord(val nextPageToken: String, val correlationId: String, val message: String, val status: String)

/** Hands records to acme.customers.v1.ConsentRecordValidationService. */
class ReplayFuelTransactionReportsTokenConsentRecordValidation(private val stub: ConsentRecordValidationServiceGrpcKt.ConsentRecordValidationServiceCoroutineStub) : ReplayFuelTransactionReportsTokenDestination {
    override suspend fun transmit(rec: ReplayFuelTransactionReportsTokenRecord) {
        stub.getConsentRecordRules(getConsentRecordRulesRequest { this.consentRecordId = rec.correlationId })
    }
}

/** Writes records to acme.customers.v1.CustomerSegmentApprovalService. */
class ReplayFuelTransactionReportsTokenCustomerSegmentApproval(private val stub: CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub) : ReplayFuelTransactionReportsTokenDestination {
    override suspend fun transmit(rec: ReplayFuelTransactionReportsTokenRecord) {
        val request = recallCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.customerSegmentId = rec.correlationId }
        }
        stub.recallCustomerSegment(request)
    }
}

/** A sink and the kind of record it takes. */
class ReplayFuelTransactionReportsTokenRegistration(val kind: String, val sink: ReplayFuelTransactionReportsTokenDestination)
