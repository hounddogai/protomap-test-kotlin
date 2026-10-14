package com.acme.transport.ops.freightpayreplicator

import com.acme.documents.v1.DocumentServiceGrpcKt
import com.acme.documents.v1.getDocumentRequest
import com.acme.fuel.v1.FuelPolicyApprovalServiceGrpcKt
import com.acme.fuel.v1.SubmitFuelPolicyRequestKt
import com.acme.fuel.v1.submitFuelPolicyRequest
import com.acme.modelregistry.v1.RegisteredModelQueryServiceGrpcKt
import com.acme.modelregistry.v1.countRegisteredModelsRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.treasury.v1.CashPositionCalculationServiceGrpcKt
import com.acme.treasury.v1.explainCashPositionCalculationRequest
import io.grpc.ManagedChannel

/** Reads acme.modelregistry.v1.RegisteredModelQueryService/CountRegisteredModels and hands what it keeps to the sink of its kind. */
suspend fun replayRegisteredModelsStatus(channel: ManagedChannel) {
    val reader = RegisteredModelQueryServiceGrpcKt.RegisteredModelQueryServiceCoroutineStub(channel)
    val reply = reader.countRegisteredModels(countRegisteredModelsRequest { this.orderBy = "default-replay-registered-models-status" })
    val record = ReplayRegisteredModelsStatusRecord(
        status = reply.result.status,
        correlationId = normalize(reply.result.correlationId),
    )
    val sinks = listOf<ReplayRegisteredModelsStatusDestination>(
        ReplayRegisteredModelsStatusCashPositionCalculation(CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub(channel)),
        ReplayRegisteredModelsStatusDocument(DocumentServiceGrpcKt.DocumentServiceCoroutineStub(channel)),
        ReplayRegisteredModelsStatusFuelPolicyApproval(FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub(channel)),
    )
    sinks[0].relay(record)
}

/** Where replayRegisteredModelsStatus hands each record. */
interface ReplayRegisteredModelsStatusDestination {
    suspend fun relay(rec: ReplayRegisteredModelsStatusRecord)
}

/** What replayRegisteredModelsStatus keeps of each CountRegisteredModels response. */
data class ReplayRegisteredModelsStatusRecord(val status: String, val correlationId: String)

/** Passes records to acme.treasury.v1.CashPositionCalculationService. */
class ReplayRegisteredModelsStatusCashPositionCalculation(private val stub: CashPositionCalculationServiceGrpcKt.CashPositionCalculationServiceCoroutineStub) : ReplayRegisteredModelsStatusDestination {
    override suspend fun relay(rec: ReplayRegisteredModelsStatusRecord) {
        RpcBoundary.run("acme.treasury.v1.CashPositionCalculationService/ExplainCashPositionCalculation") { stub.explainCashPositionCalculation(explainCashPositionCalculationRequest { this.reason = rec.correlationId }) }
    }
}

/** Hands records to acme.documents.v1.DocumentService. */
class ReplayRegisteredModelsStatusDocument(private val stub: DocumentServiceGrpcKt.DocumentServiceCoroutineStub) : ReplayRegisteredModelsStatusDestination {
    override suspend fun relay(rec: ReplayRegisteredModelsStatusRecord) {
        RpcBoundary.run("acme.documents.v1.DocumentService/GetDocument") { stub.getDocument(getDocumentRequest { this.documentId = rec.status }) }
    }
}

/** Sends records to acme.fuel.v1.FuelPolicyApprovalService. */
class ReplayRegisteredModelsStatusFuelPolicyApproval(private val stub: FuelPolicyApprovalServiceGrpcKt.FuelPolicyApprovalServiceCoroutineStub) : ReplayRegisteredModelsStatusDestination {
    override suspend fun relay(rec: ReplayRegisteredModelsStatusRecord) {
        val request = submitFuelPolicyRequest {
            this.details = SubmitFuelPolicyRequestKt.details { this.comment = rec.status }
        }
        stub.submitFuelPolicy(request)
    }
}
