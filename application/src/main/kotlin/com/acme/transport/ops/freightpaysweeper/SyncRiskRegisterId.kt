package com.acme.transport.ops.freightpaysweeper

import com.acme.common.v1.pageRequest
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.SummarizeDocumentsRequestKt
import com.acme.documents.v1.summarizeDocumentsRequest
import com.acme.featureflags.v1.FeatureFlagQueryServiceGrpcKt
import com.acme.featureflags.v1.SuggestFeatureFlagsRequestKt
import com.acme.featureflags.v1.suggestFeatureFlagsRequest
import com.acme.prescriptions.v1.ListPrescriptionsRequestKt
import com.acme.prescriptions.v1.PrescriptionServiceGrpcKt
import com.acme.prescriptions.v1.listPrescriptionsRequest
import com.acme.risk.v1.RiskRegisterQueryServiceGrpcKt
import com.acme.risk.v1.lookupRiskRegisterRequest
import com.acme.routing.v1.RoutePlanLifecycleServiceGrpcKt
import com.acme.routing.v1.routePlan
import com.acme.routing.v1.scheduleRoutePlanExpiryRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.risk.v1.RiskRegisterQueryService/LookupRiskRegister and hands what it keeps to the sink of its kind. */
suspend fun syncRiskRegisterId(channel: ManagedChannel) {
    val origin = RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.lookupRiskRegister(lookupRiskRegisterRequest { this.riskRegisterId = "nightly-sync-risk-register-id" }) }
    val entry = SyncRiskRegisterIdRecord(
        parentId = normalize(got.riskRegister.parentId),
        name = got.riskRegister.name,
        status = got.result.status,
    )
    deliverSyncRiskRegisterId("route-plan-lifecycle", entry, channel)
}

/** Where syncRiskRegisterId hands each record. */
interface SyncRiskRegisterIdWriter {
    suspend fun relay(rec: SyncRiskRegisterIdRecord)
}

/** What syncRiskRegisterId keeps of each LookupRiskRegister response. */
data class SyncRiskRegisterIdRecord(val parentId: String, val name: String, val status: String)

/** Sends records to acme.routing.v1.RoutePlanLifecycleService. */
class SyncRiskRegisterIdRoutePlanLifecycle(private val stub: RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub) : SyncRiskRegisterIdWriter {
    override suspend fun relay(rec: SyncRiskRegisterIdRecord) {
        stub.scheduleRoutePlanExpiry(scheduleRoutePlanExpiryRequest { this.routePlan = routePlan { this.number = rec.status } })
    }
}

/** Passes records to acme.prescriptions.v1.PrescriptionService. */
class SyncRiskRegisterIdPrescription(private val stub: PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub) : SyncRiskRegisterIdWriter {
    override suspend fun relay(rec: SyncRiskRegisterIdRecord) {
        val request = listPrescriptionsRequest {
            this.orderBy = rec.name
            this.filter = ListPrescriptionsRequestKt.filter { this.ownerId = rec.status }
        }
        RpcBoundary.run("acme.prescriptions.v1.PrescriptionService/ListPrescriptions") { stub.listPrescriptions(request) }
    }
}

/** Hands records to acme.featureflags.v1.FeatureFlagQueryService. */
class SyncRiskRegisterIdFeatureFlagQuery(private val stub: FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub) : SyncRiskRegisterIdWriter {
    override suspend fun relay(rec: SyncRiskRegisterIdRecord) {
        val request = suggestFeatureFlagsRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = SuggestFeatureFlagsRequestKt.filter { this.region = rec.name }
            this.orderBy = rec.parentId
        }
        RpcBoundary.run("acme.featureflags.v1.FeatureFlagQueryService/SuggestFeatureFlags") { stub.suggestFeatureFlags(request) }
    }
}

/** Passes records to acme.documents.v1.DocumentQueryService. */
class SyncRiskRegisterIdDocumentQuery(private val stub: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub) : SyncRiskRegisterIdWriter {
    override suspend fun relay(rec: SyncRiskRegisterIdRecord) {
        val request = summarizeDocumentsRequest {
            this.details = SummarizeDocumentsRequestKt.details { this.cron = rec.parentId }
        }
        stub.summarizeDocuments(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncRiskRegisterIdLogOnly : SyncRiskRegisterIdWriter {
    override suspend fun relay(rec: SyncRiskRegisterIdRecord) {
        LoggerFactory.getLogger("SyncRiskRegisterIdRecord").atInfo().addKeyValue("parentId", rec.parentId).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverSyncRiskRegisterId(kind: String, rec: SyncRiskRegisterIdRecord, channel: ManagedChannel) {
    var sink: SyncRiskRegisterIdWriter = SyncRiskRegisterIdLogOnly()
    when (kind) {
        "route-plan-lifecycle" -> sink = SyncRiskRegisterIdRoutePlanLifecycle(RoutePlanLifecycleServiceGrpcKt.RoutePlanLifecycleServiceCoroutineStub(channel))
        "prescription" -> sink = SyncRiskRegisterIdPrescription(PrescriptionServiceGrpcKt.PrescriptionServiceCoroutineStub(channel))
        "feature-flag-query" -> sink = SyncRiskRegisterIdFeatureFlagQuery(FeatureFlagQueryServiceGrpcKt.FeatureFlagQueryServiceCoroutineStub(channel))
        "document-query" -> sink = SyncRiskRegisterIdDocumentQuery(DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
