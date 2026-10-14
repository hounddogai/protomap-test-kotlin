package com.acme.transport.ops.lastmilesync

import com.acme.certificates.v1.CertificateServiceGrpcKt
import com.acme.certificates.v1.getCertificateRequest
import com.acme.fleet.v1.InspectionApprovalServiceGrpcKt
import com.acme.fleet.v1.escalateInspectionRequest
import com.acme.inventory.v1.StockItemQueryServiceGrpcKt
import com.acme.inventory.v1.browseStockItemsRequest
import com.acme.onboarding.v1.CalculateOnboardingPlanRequestKt
import com.acme.onboarding.v1.OnboardingPlanCalculationServiceGrpcKt
import com.acme.onboarding.v1.calculateOnboardingPlanRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.inventory.v1.StockItemQueryService/BrowseStockItems and hands what it keeps to the sink of its kind. */
suspend fun syncStockItemsStatus(channel: ManagedChannel) {
    val source = StockItemQueryServiceGrpcKt.StockItemQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { source.browseStockItems(browseStockItemsRequest { this.orderBy = "default-sync-stock-items-status" }) }
    val record = SyncStockItemsStatusRecord(
        status = got.result.status,
        correlationId = got.result.correlationId,
    )
    actOnSyncStockItemsStatus("onboarding-plan-calculation", record, channel)
}

/** Where syncStockItemsStatus hands each record. */
interface SyncStockItemsStatusSink {
    suspend fun transmit(rec: SyncStockItemsStatusRecord)
}

/** What syncStockItemsStatus keeps of each BrowseStockItems response. */
data class SyncStockItemsStatusRecord(val status: String, val correlationId: String)

/** Writes records to acme.onboarding.v1.OnboardingPlanCalculationService. */
class SyncStockItemsStatusOnboardingPlanCalculation(private val stub: OnboardingPlanCalculationServiceGrpcKt.OnboardingPlanCalculationServiceCoroutineStub) : SyncStockItemsStatusSink {
    override suspend fun transmit(rec: SyncStockItemsStatusRecord) {
        val request = calculateOnboardingPlanRequest {
            this.details = CalculateOnboardingPlanRequestKt.details { this.currencyCode = rec.correlationId }
        }
        RpcBoundary.run("acme.onboarding.v1.OnboardingPlanCalculationService/CalculateOnboardingPlan") { stub.calculateOnboardingPlan(request) }
    }
}

/** Hands records to acme.certificates.v1.CertificateService. */
class SyncStockItemsStatusCertificate(private val stub: CertificateServiceGrpcKt.CertificateServiceCoroutineStub) : SyncStockItemsStatusSink {
    override suspend fun transmit(rec: SyncStockItemsStatusRecord) {
        stub.getCertificate(getCertificateRequest { this.certificateId = rec.correlationId })
    }
}

/** Hands records to acme.fleet.v1.InspectionApprovalService. */
class SyncStockItemsStatusInspectionApproval(private val stub: InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub) : SyncStockItemsStatusSink {
    override suspend fun transmit(rec: SyncStockItemsStatusRecord) {
        stub.escalateInspection(escalateInspectionRequest { this.inspectionId = rec.correlationId })
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSyncStockItemsStatus(kind: String, rec: SyncStockItemsStatusRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "onboarding-plan-calculation" to { SyncStockItemsStatusOnboardingPlanCalculation(OnboardingPlanCalculationServiceGrpcKt.OnboardingPlanCalculationServiceCoroutineStub(channel)).transmit(rec) },
        "certificate" to { SyncStockItemsStatusCertificate(CertificateServiceGrpcKt.CertificateServiceCoroutineStub(channel)).transmit(rec) },
        "inspection-approval" to { SyncStockItemsStatusInspectionApproval(InspectionApprovalServiceGrpcKt.InspectionApprovalServiceCoroutineStub(channel)).transmit(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
