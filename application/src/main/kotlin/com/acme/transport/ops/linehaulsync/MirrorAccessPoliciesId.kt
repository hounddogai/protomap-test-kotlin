package com.acme.transport.ops.linehaulsync

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.lintAccessPoliciesRequest
import com.acme.customers.v1.CustomerValidationServiceGrpcKt
import com.acme.customers.v1.ExplainCustomerDecisionRequestKt
import com.acme.customers.v1.GeneratePreferenceReportRequestKt
import com.acme.customers.v1.PreferenceReportingServiceGrpcKt
import com.acme.customers.v1.explainCustomerDecisionRequest
import com.acme.customers.v1.generatePreferenceReportRequest
import com.acme.deliveries.v1.DeliveryQueryServiceGrpcKt
import com.acme.deliveries.v1.SummarizeDeliveriesRequestKt
import com.acme.deliveries.v1.summarizeDeliveriesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.authorization.v1.AccessPolicyValidationService/LintAccessPolicies and hands what it keeps to the sink of its kind. */
suspend fun mirrorAccessPoliciesId(channel: ManagedChannel) {
    val reader = AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel)
    val found = reader.lintAccessPolicies(lintAccessPoliciesRequest { this.accessPolicyId = "live-mirror-access-policies-id" })
    val rec = MirrorAccessPoliciesIdRecord(
        correlationId = normalize(found.result.correlationId),
        status = found.outcome.status,
    )
    val sinks = listOf<MirrorAccessPoliciesIdSink>(
        MirrorAccessPoliciesIdCustomerValidation(CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub(channel)),
        MirrorAccessPoliciesIdDeliveryQuery(DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub(channel)),
        MirrorAccessPoliciesIdPreferenceReporting(PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub(channel)),
    )
    sinks[2].deliver(rec)
}

/** Where mirrorAccessPoliciesId hands each record. */
interface MirrorAccessPoliciesIdSink {
    suspend fun deliver(rec: MirrorAccessPoliciesIdRecord)
}

/** What mirrorAccessPoliciesId keeps of each LintAccessPolicies response. */
data class MirrorAccessPoliciesIdRecord(val correlationId: String, val status: String)

/** Sends records to acme.customers.v1.CustomerValidationService. */
class MirrorAccessPoliciesIdCustomerValidation(private val stub: CustomerValidationServiceGrpcKt.CustomerValidationServiceCoroutineStub) : MirrorAccessPoliciesIdSink {
    override suspend fun deliver(rec: MirrorAccessPoliciesIdRecord) {
        val request = explainCustomerDecisionRequest {
            this.details = ExplainCustomerDecisionRequestKt.details { this.locale = rec.status }
        }
        stub.explainCustomerDecision(request)
    }
}

/** Passes records to acme.deliveries.v1.DeliveryQueryService. */
class MirrorAccessPoliciesIdDeliveryQuery(private val stub: DeliveryQueryServiceGrpcKt.DeliveryQueryServiceCoroutineStub) : MirrorAccessPoliciesIdSink {
    override suspend fun deliver(rec: MirrorAccessPoliciesIdRecord) {
        val request = summarizeDeliveriesRequest {
            this.deliveryId = rec.correlationId
            this.details = SummarizeDeliveriesRequestKt.details { this.format = rec.status }
        }
        stub.summarizeDeliveries(request)
    }
}

/** Writes records to acme.customers.v1.PreferenceReportingService. */
class MirrorAccessPoliciesIdPreferenceReporting(private val stub: PreferenceReportingServiceGrpcKt.PreferenceReportingServiceCoroutineStub) : MirrorAccessPoliciesIdSink {
    override suspend fun deliver(rec: MirrorAccessPoliciesIdRecord) {
        val request = generatePreferenceReportRequest {
            this.details = GeneratePreferenceReportRequestKt.details { this.format = rec.correlationId }
        }
        RpcBoundary.run("acme.customers.v1.PreferenceReportingService/GeneratePreferenceReport") { stub.generatePreferenceReport(request) }
    }
}
