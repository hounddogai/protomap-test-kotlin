package com.acme.transport.ops.freightpaysweeper

import com.acme.customers.v1.BrowseCustomersRequestKt
import com.acme.customers.v1.CustomerQueryServiceGrpcKt
import com.acme.customers.v1.CustomerSegmentCalculationServiceGrpcKt
import com.acme.customers.v1.browseCustomersRequest
import com.acme.customers.v1.customerSegment
import com.acme.customers.v1.simulateCustomerSegmentRequest
import com.acme.sponsorships.v1.CompareSponsorOptionsRequestKt
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.compareSponsorOptionsRequest
import com.acme.sponsorships.v1.sponsor
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardValidationServiceGrpcKt
import com.acme.yards.v1.validateYardRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.YardValidationService/ValidateYard and hands what it keeps to the sink of its kind. */
suspend fun handOffYardId(channel: ManagedChannel) {
    val source = YardValidationServiceGrpcKt.YardValidationServiceCoroutineStub(channel)
    val found = source.validateYard(validateYardRequest { this.yardId = "main-hand-off-yard-id" })
    val record = HandOffYardIdRecord(
        yardId = found.yard.yardId,
        code = found.yard.code,
    )
    val sinks = mapOf<String, HandOffYardIdWriter>(
        "sponsor-calculation" to HandOffYardIdSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel)),
        "customer-segment-calculation" to HandOffYardIdCustomerSegmentCalculation(CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub(channel)),
        "customer-query" to HandOffYardIdCustomerQuery(CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "customer-query") continue
        sinks[kind]?.relay(record)
    }
}

/** Where handOffYardId hands each record. */
interface HandOffYardIdWriter {
    suspend fun relay(rec: HandOffYardIdRecord)
}

/** What handOffYardId keeps of each ValidateYard response. */
data class HandOffYardIdRecord(val yardId: String, val code: String)

/** Passes records to acme.sponsorships.v1.SponsorCalculationService. */
class HandOffYardIdSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : HandOffYardIdWriter {
    override suspend fun relay(rec: HandOffYardIdRecord) {
        val request = compareSponsorOptionsRequest {
            this.sponsor = sponsor { this.sponsorId = rec.yardId }
            this.details = CompareSponsorOptionsRequestKt.details { this.currencyCode = rec.code }
        }
        RpcBoundary.run("acme.sponsorships.v1.SponsorCalculationService/CompareSponsorOptions") { stub.compareSponsorOptions(request) }
    }
}

/** Writes records to acme.customers.v1.CustomerSegmentCalculationService. */
class HandOffYardIdCustomerSegmentCalculation(private val stub: CustomerSegmentCalculationServiceGrpcKt.CustomerSegmentCalculationServiceCoroutineStub) : HandOffYardIdWriter {
    override suspend fun relay(rec: HandOffYardIdRecord) {
        val request = simulateCustomerSegmentRequest {
            this.customerSegment = customerSegment { this.customerSegmentId = rec.yardId }
        }
        RpcBoundary.run("acme.customers.v1.CustomerSegmentCalculationService/SimulateCustomerSegment") { stub.simulateCustomerSegment(request) }
    }
}

/** Passes records to acme.customers.v1.CustomerQueryService. */
class HandOffYardIdCustomerQuery(private val stub: CustomerQueryServiceGrpcKt.CustomerQueryServiceCoroutineStub) : HandOffYardIdWriter {
    override suspend fun relay(rec: HandOffYardIdRecord) {
        val request = browseCustomersRequest {
            this.filter = BrowseCustomersRequestKt.filter { this.ownerId = rec.yardId }
        }
        stub.browseCustomers(request)
    }
}
