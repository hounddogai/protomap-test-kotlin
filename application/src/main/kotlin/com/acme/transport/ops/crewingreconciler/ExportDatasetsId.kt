package com.acme.transport.ops.crewingreconciler

import com.acme.chat.v1.ChatRoomCalculationServiceGrpcKt
import com.acme.chat.v1.EstimateChatRoomRequestKt
import com.acme.chat.v1.estimateChatRoomRequest
import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.summarizeDatasetsRequest
import com.acme.featureflags.v1.CheckFeatureFlagEligibilityRequestKt
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.checkFeatureFlagEligibilityRequest
import com.acme.featureflags.v1.featureFlag
import com.acme.fleet.v1.CountVehiclesRequestKt
import com.acme.fleet.v1.VehicleQueryServiceGrpcKt
import com.acme.fleet.v1.countVehiclesRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.datasets.v1.DatasetQueryService/SummarizeDatasets and hands what it keeps to the sink of its kind. */
suspend fun exportDatasetsId(channel: ManagedChannel) {
    val upstream = DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub(channel)
    val found = RpcBoundary.read { upstream.summarizeDatasets(summarizeDatasetsRequest { this.reason = "main-export-datasets-id" }) }
    val row = ExportDatasetsIdRecord(
        correlationId = found.result.correlationId,
        reportId = found.reportId,
    )
    dispatchExportDatasetsId("vehicle-query", row, channel)
}

/** Where exportDatasetsId hands each record. */
interface ExportDatasetsIdSink {
    suspend fun hand(rec: ExportDatasetsIdRecord)
}

/** What exportDatasetsId keeps of each SummarizeDatasets response. */
data class ExportDatasetsIdRecord(val correlationId: String, val reportId: String)

/** Sends records to acme.chat.v1.ChatRoomCalculationService. */
class ExportDatasetsIdChatRoomCalculation(private val stub: ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub) : ExportDatasetsIdSink {
    override suspend fun hand(rec: ExportDatasetsIdRecord) {
        val request = estimateChatRoomRequest {
            this.details = EstimateChatRoomRequestKt.details { this.currencyCode = rec.correlationId }
        }
        stub.estimateChatRoom(request)
    }
}

/** Writes records to acme.fleet.v1.VehicleQueryService. */
class ExportDatasetsIdVehicleQuery(private val stub: VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub) : ExportDatasetsIdSink {
    override suspend fun hand(rec: ExportDatasetsIdRecord) {
        val request = countVehiclesRequest {
            this.filter = CountVehiclesRequestKt.filter { this.ownerId = rec.reportId; this.query = rec.correlationId }
        }
        stub.countVehicles(request)
    }
}

/** Hands records to acme.featureflags.v1.FeatureFlagValidationService. */
class ExportDatasetsIdFeatureFlagValidation(private val stub: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub) : ExportDatasetsIdSink {
    override suspend fun hand(rec: ExportDatasetsIdRecord) {
        val request = checkFeatureFlagEligibilityRequest {
            this.featureFlag = featureFlag { this.description = rec.reportId }
            this.details = CheckFeatureFlagEligibilityRequestKt.details { this.locale = rec.correlationId }
        }
        stub.checkFeatureFlagEligibility(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportDatasetsIdLogOnly : ExportDatasetsIdSink {
    override suspend fun hand(rec: ExportDatasetsIdRecord) {
        LoggerFactory.getLogger("ExportDatasetsIdRecord").atInfo().addKeyValue("correlationId", rec.correlationId).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchExportDatasetsId(kind: String, rec: ExportDatasetsIdRecord, channel: ManagedChannel) {
    var write: suspend (ExportDatasetsIdRecord) -> Unit = ExportDatasetsIdLogOnly()::hand
    when (kind) {
        "chat-room-calculation" -> write = ExportDatasetsIdChatRoomCalculation(ChatRoomCalculationServiceGrpcKt.ChatRoomCalculationServiceCoroutineStub(channel))::hand
        "vehicle-query" -> write = ExportDatasetsIdVehicleQuery(VehicleQueryServiceGrpcKt.VehicleQueryServiceCoroutineStub(channel))::hand
        "feature-flag-validation" -> write = ExportDatasetsIdFeatureFlagValidation(FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(channel))::hand
    }
    write(rec)
}
