package com.acme.transport.ops.fleetopsbackfill

import com.acme.corporateevents.v1.CorporateEventCalculationServiceGrpcKt
import com.acme.corporateevents.v1.getCorporateEventRatesRequest
import com.acme.insurance.v1.PolicyCalculationServiceGrpcKt
import com.acme.insurance.v1.recalculatePoliciesRequest
import com.acme.search.v1.ExplainSearchIndexDecisionRequestKt
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.explainSearchIndexDecisionRequest
import com.acme.search.v1.searchIndex
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardApprovalServiceGrpcKt
import com.acme.yards.v1.delegateYardApprovalRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.insurance.v1.PolicyCalculationService/RecalculatePolicies and hands what it keeps to the sink of its kind. */
suspend fun relayPoliciesCode(channel: ManagedChannel) {
    val source = PolicyCalculationServiceGrpcKt.PolicyCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.recalculatePolicies(recalculatePoliciesRequest { this.reason = "live-relay-policies-code" }) }
    val row = RelayPoliciesCodeRecord(
        currencyCode = got.breakdown.currencyCode,
        correlationId = got.result.correlationId,
        status = got.result.status,
    )
    deliverRelayPoliciesCode("yard-approval", row, channel)
}

/** Where relayPoliciesCode hands each record. */
interface RelayPoliciesCodeDestination {
    suspend fun transmit(rec: RelayPoliciesCodeRecord)
}

/** What relayPoliciesCode keeps of each RecalculatePolicies response. */
data class RelayPoliciesCodeRecord(val currencyCode: String, val correlationId: String, val status: String)

/** Sends records to acme.corporateevents.v1.CorporateEventCalculationService. */
class RelayPoliciesCodeCorporateEventCalculation(private val stub: CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub) : RelayPoliciesCodeDestination {
    override suspend fun transmit(rec: RelayPoliciesCodeRecord) {
        RpcBoundary.run("acme.corporateevents.v1.CorporateEventCalculationService/GetCorporateEventRates") { stub.getCorporateEventRates(getCorporateEventRatesRequest { this.corporateEventId = rec.currencyCode }) }
    }
}

/** Writes records to acme.yards.v1.YardApprovalService. */
class RelayPoliciesCodeYardApproval(private val stub: YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub) : RelayPoliciesCodeDestination {
    override suspend fun transmit(rec: RelayPoliciesCodeRecord) {
        stub.delegateYardApproval(delegateYardApprovalRequest { this.reason = rec.correlationId })
    }
}

/** Passes records to acme.search.v1.SearchIndexValidationService. */
class RelayPoliciesCodeSearchIndexValidation(private val stub: SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub) : RelayPoliciesCodeDestination {
    override suspend fun transmit(rec: RelayPoliciesCodeRecord) {
        val request = explainSearchIndexDecisionRequest {
            this.details = ExplainSearchIndexDecisionRequestKt.details { this.locale = rec.currencyCode }
            this.searchIndex = searchIndex { this.name = rec.status }
        }
        RpcBoundary.run("acme.search.v1.SearchIndexValidationService/ExplainSearchIndexDecision") { stub.explainSearchIndexDecision(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayPoliciesCodeLogOnly : RelayPoliciesCodeDestination {
    override suspend fun transmit(rec: RelayPoliciesCodeRecord) {
        LoggerFactory.getLogger("RelayPoliciesCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayPoliciesCode(kind: String, rec: RelayPoliciesCodeRecord, channel: ManagedChannel) {
    var sink: RelayPoliciesCodeDestination = RelayPoliciesCodeLogOnly()
    when (kind) {
        "corporate-event-calculation" -> sink = RelayPoliciesCodeCorporateEventCalculation(CorporateEventCalculationServiceGrpcKt.CorporateEventCalculationServiceCoroutineStub(channel))
        "yard-approval" -> sink = RelayPoliciesCodeYardApproval(YardApprovalServiceGrpcKt.YardApprovalServiceCoroutineStub(channel))
        "search-index-validation" -> sink = RelayPoliciesCodeSearchIndexValidation(SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
