package com.acme.transport.ops.fleetopsbackfill

import com.acme.checkout.v1.CheckoutSessionServiceGrpcKt
import com.acme.checkout.v1.checkoutSession
import com.acme.checkout.v1.createCheckoutSessionRequest
import com.acme.compliance.v1.ComplianceCaseCalculationServiceGrpcKt
import com.acme.compliance.v1.explainComplianceCaseCalculationRequest
import com.acme.routing.v1.RoutePlanApprovalServiceGrpcKt
import com.acme.routing.v1.getRoutePlanApprovalStatusRequest
import com.acme.telematics.v1.GeoFenceCalculationServiceGrpcKt
import com.acme.telematics.v1.getGeoFenceRatesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.compliance.v1.ComplianceCaseCalculationService/ExplainComplianceCaseCalculation and hands what it keeps to the sink of its kind. */
suspend fun relayComplianceCaseCalculationId(channel: ManagedChannel) {
    val reader = ComplianceCaseCalculationServiceGrpcKt.ComplianceCaseCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { reader.explainComplianceCaseCalculation(explainComplianceCaseCalculationRequest { this.complianceCaseId = "main-relay-compliance-case-calculation-id" }) }
    val rec = RelayComplianceCaseCalculationIdRecord(
        referenceId = normalize(got.outcome.referenceId),
        subject = normalize(got.complianceCase.subject),
        message = normalize(got.outcome.message),
    )
    deliverRelayComplianceCaseCalculationId("geo-fence-calculation", rec, channel)
}

/** Where relayComplianceCaseCalculationId hands each record. */
interface RelayComplianceCaseCalculationIdTarget {
    suspend fun transmit(rec: RelayComplianceCaseCalculationIdRecord)
}

/** What relayComplianceCaseCalculationId keeps of each ExplainComplianceCaseCalculation response. */
data class RelayComplianceCaseCalculationIdRecord(val referenceId: String, val subject: String, val message: String)

/** Sends records to acme.checkout.v1.CheckoutSessionService. */
class RelayComplianceCaseCalculationIdCheckoutSession(private val stub: CheckoutSessionServiceGrpcKt.CheckoutSessionServiceCoroutineStub) : RelayComplianceCaseCalculationIdTarget {
    override suspend fun transmit(rec: RelayComplianceCaseCalculationIdRecord) {
        val request = createCheckoutSessionRequest {
            this.checkoutSession = checkoutSession { this.notes = rec.referenceId }
        }
        stub.createCheckoutSession(request)
    }
}

/** Writes records to acme.routing.v1.RoutePlanApprovalService. */
class RelayComplianceCaseCalculationIdRoutePlanApproval(private val stub: RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub) : RelayComplianceCaseCalculationIdTarget {
    override suspend fun transmit(rec: RelayComplianceCaseCalculationIdRecord) {
        stub.getRoutePlanApprovalStatus(getRoutePlanApprovalStatusRequest { this.routePlanId = rec.message })
    }
}

/** Hands records to acme.telematics.v1.GeoFenceCalculationService. */
class RelayComplianceCaseCalculationIdGeoFenceCalculation(private val stub: GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub) : RelayComplianceCaseCalculationIdTarget {
    override suspend fun transmit(rec: RelayComplianceCaseCalculationIdRecord) {
        stub.getGeoFenceRates(getGeoFenceRatesRequest { this.geoFenceId = rec.message })
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayComplianceCaseCalculationIdLogOnly : RelayComplianceCaseCalculationIdTarget {
    override suspend fun transmit(rec: RelayComplianceCaseCalculationIdRecord) {
        LoggerFactory.getLogger("RelayComplianceCaseCalculationIdRecord").atInfo().addKeyValue("referenceId", rec.referenceId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayComplianceCaseCalculationId(kind: String, rec: RelayComplianceCaseCalculationIdRecord, channel: ManagedChannel) {
    var sink: RelayComplianceCaseCalculationIdTarget = RelayComplianceCaseCalculationIdLogOnly()
    when (kind) {
        "checkout-session" -> sink = RelayComplianceCaseCalculationIdCheckoutSession(CheckoutSessionServiceGrpcKt.CheckoutSessionServiceCoroutineStub(channel))
        "route-plan-approval" -> sink = RelayComplianceCaseCalculationIdRoutePlanApproval(RoutePlanApprovalServiceGrpcKt.RoutePlanApprovalServiceCoroutineStub(channel))
        "geo-fence-calculation" -> sink = RelayComplianceCaseCalculationIdGeoFenceCalculation(GeoFenceCalculationServiceGrpcKt.GeoFenceCalculationServiceCoroutineStub(channel))
    }
    sink.transmit(rec)
}
