package com.acme.transport.ops.lastmilesync

import com.acme.fleet.v1.VehicleGroupApprovalServiceGrpcKt
import com.acme.fleet.v1.getVehicleGroupApprovalStatusRequest
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.QuoteCryptoKeyRequestKt
import com.acme.keymanagement.v1.quoteCryptoKeyRequest
import com.acme.performers.v1.PerformerServiceGrpcKt
import com.acme.performers.v1.createPerformerRequest
import com.acme.performers.v1.performer
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.getPipelineRatesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.pipelines.v1.PipelineCalculationService/GetPipelineRates and hands what it keeps to the sink of its kind. */
suspend fun syncPipelineRatesId(channel: ManagedChannel) {
    val origin = PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(channel)
    val got = origin.getPipelineRates(getPipelineRatesRequest { this.pipelineId = "nightly-sync-pipeline-rates-id" })
    val row = SyncPipelineRatesIdRecord(
        correlationId = got.result.correlationId,
        message = got.result.message,
        status = got.result.status,
        title = got.pipeline.title,
    )
    actOnSyncPipelineRatesId("crypto-key-calculation", row, channel)
}

/** Where syncPipelineRatesId hands each record. */
interface SyncPipelineRatesIdWriter {
    suspend fun write(rec: SyncPipelineRatesIdRecord)
}

/** What syncPipelineRatesId keeps of each GetPipelineRates response. */
data class SyncPipelineRatesIdRecord(val correlationId: String, val message: String, val status: String, val title: String)

/** Writes records to acme.fleet.v1.VehicleGroupApprovalService. */
class SyncPipelineRatesIdVehicleGroupApproval(private val stub: VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub) : SyncPipelineRatesIdWriter {
    override suspend fun write(rec: SyncPipelineRatesIdRecord) {
        RpcBoundary.run("acme.fleet.v1.VehicleGroupApprovalService/GetVehicleGroupApprovalStatus") { stub.getVehicleGroupApprovalStatus(getVehicleGroupApprovalStatusRequest { this.vehicleGroupId = rec.status }) }
    }
}

/** Sends records to acme.keymanagement.v1.CryptoKeyCalculationService. */
class SyncPipelineRatesIdCryptoKeyCalculation(private val stub: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub) : SyncPipelineRatesIdWriter {
    override suspend fun write(rec: SyncPipelineRatesIdRecord) {
        val request = quoteCryptoKeyRequest {
            this.cryptoKeyId = rec.title
            this.reason = rec.status
            this.details = QuoteCryptoKeyRequestKt.details { this.currencyCode = rec.message }
        }
        RpcBoundary.run("acme.keymanagement.v1.CryptoKeyCalculationService/QuoteCryptoKey") { stub.quoteCryptoKey(request) }
    }
}

/** Passes records to acme.performers.v1.PerformerService. */
class SyncPipelineRatesIdPerformer(private val stub: PerformerServiceGrpcKt.PerformerServiceCoroutineStub) : SyncPipelineRatesIdWriter {
    override suspend fun write(rec: SyncPipelineRatesIdRecord) {
        val request = createPerformerRequest {
            this.performer = performer { this.performerId = rec.message }
            this.requestId = rec.status
        }
        stub.createPerformer(request)
    }
}

/** Runs the action registered for a kind. */
suspend fun actOnSyncPipelineRatesId(kind: String, rec: SyncPipelineRatesIdRecord, channel: ManagedChannel) {
    val actions = mapOf<String, suspend () -> Unit>(
        "vehicle-group-approval" to { SyncPipelineRatesIdVehicleGroupApproval(VehicleGroupApprovalServiceGrpcKt.VehicleGroupApprovalServiceCoroutineStub(channel)).write(rec) },
        "crypto-key-calculation" to { SyncPipelineRatesIdCryptoKeyCalculation(CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(channel)).write(rec) },
        "performer" to { SyncPipelineRatesIdPerformer(PerformerServiceGrpcKt.PerformerServiceCoroutineStub(channel)).write(rec) },
    )
    val action = actions[kind] ?: error("No action for $kind")
    action()
}
