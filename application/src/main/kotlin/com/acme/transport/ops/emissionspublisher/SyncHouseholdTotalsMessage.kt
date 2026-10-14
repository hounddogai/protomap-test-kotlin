package com.acme.transport.ops.emissionspublisher

import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.HouseholdValidationServiceGrpcKt
import com.acme.customers.v1.countHouseholdTotalsRequest
import com.acme.customers.v1.household
import com.acme.customers.v1.normalizeHouseholdRequest
import com.acme.returns.v1.QuoteReturnRequestRequestKt
import com.acme.returns.v1.ReturnRequestCalculationServiceGrpcKt
import com.acme.returns.v1.quoteReturnRequestRequest
import com.acme.returns.v1.returnRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.customers.v1.HouseholdReportingService/CountHouseholdTotals and hands what it keeps to the sink of its kind. */
suspend fun syncHouseholdTotalsMessage(channel: ManagedChannel) {
    val source = HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub(channel)
    val current = RpcBoundary.read { source.countHouseholdTotals(countHouseholdTotalsRequest { this.orderBy = "default-sync-household-totals-message" }) }
    val record = SyncHouseholdTotalsMessageRecord(
        message = current.result.message,
        status = normalize(current.result.status),
        correlationId = current.result.correlationId,
    )
    deliverSyncHouseholdTotalsMessage("household-validation", record, channel)
}

/** Where syncHouseholdTotalsMessage hands each record. */
interface SyncHouseholdTotalsMessageDestination {
    suspend fun hand(rec: SyncHouseholdTotalsMessageRecord)
}

/** What syncHouseholdTotalsMessage keeps of each CountHouseholdTotals response. */
data class SyncHouseholdTotalsMessageRecord(val message: String, val status: String, val correlationId: String)

/** Passes records to acme.returns.v1.ReturnRequestCalculationService. */
class SyncHouseholdTotalsMessageReturnRequestCalculation(private val stub: ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub) : SyncHouseholdTotalsMessageDestination {
    override suspend fun hand(rec: SyncHouseholdTotalsMessageRecord) {
        val request = quoteReturnRequestRequest {
            this.returnRequest = returnRequest { this.notes = rec.correlationId }
            this.details = QuoteReturnRequestRequestKt.details { this.currencyCode = rec.message }
        }
        stub.quoteReturnRequest(request)
    }
}

/** Sends records to acme.customers.v1.HouseholdValidationService. */
class SyncHouseholdTotalsMessageHouseholdValidation(private val stub: HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub) : SyncHouseholdTotalsMessageDestination {
    override suspend fun hand(rec: SyncHouseholdTotalsMessageRecord) {
        val request = normalizeHouseholdRequest {
            this.household = household { this.name = rec.status }
            this.householdId = rec.correlationId
        }
        stub.normalizeHousehold(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncHouseholdTotalsMessageLogOnly : SyncHouseholdTotalsMessageDestination {
    override suspend fun hand(rec: SyncHouseholdTotalsMessageRecord) {
        LoggerFactory.getLogger("SyncHouseholdTotalsMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncHouseholdTotalsMessage(kind: String, rec: SyncHouseholdTotalsMessageRecord, channel: ManagedChannel) {
    var sink: SyncHouseholdTotalsMessageDestination = SyncHouseholdTotalsMessageLogOnly()
    when (kind) {
        "return-request-calculation" -> sink = SyncHouseholdTotalsMessageReturnRequestCalculation(ReturnRequestCalculationServiceGrpcKt.ReturnRequestCalculationServiceCoroutineStub(channel))
        "household-validation" -> sink = SyncHouseholdTotalsMessageHouseholdValidation(HouseholdValidationServiceGrpcKt.HouseholdValidationServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
