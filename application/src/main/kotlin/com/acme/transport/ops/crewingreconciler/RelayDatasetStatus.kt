package com.acme.transport.ops.crewingreconciler

import com.acme.authorization.v1.AccessPolicyValidationServiceGrpcKt
import com.acme.authorization.v1.CheckAccessPolicyEligibilityRequestKt
import com.acme.authorization.v1.accessPolicy
import com.acme.authorization.v1.checkAccessPolicyEligibilityRequest
import com.acme.datasets.v1.DatasetQueryServiceGrpcKt
import com.acme.datasets.v1.peekDatasetRequest
import com.acme.dispatch.v1.DispatchOrderServiceGrpcKt
import com.acme.dispatch.v1.getDispatchOrderRequest
import com.acme.fuel.v1.FuelTransactionSyncServiceGrpcKt
import com.acme.fuel.v1.SyncFuelTransactionsRequestKt
import com.acme.fuel.v1.fuelTransaction
import com.acme.fuel.v1.syncFuelTransactionsRequest
import com.acme.maintenance.v1.BrowseServiceSchedulesRequestKt
import com.acme.maintenance.v1.ServiceScheduleQueryServiceGrpcKt
import com.acme.maintenance.v1.browseServiceSchedulesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.datasets.v1.DatasetQueryService/PeekDataset and hands what it keeps to the sink of its kind. */
suspend fun relayDatasetStatus(channel: ManagedChannel) {
    val reader = DatasetQueryServiceGrpcKt.DatasetQueryServiceCoroutineStub(channel)
    val current = reader.peekDataset(peekDatasetRequest { this.datasetId = "current-relay-dataset-status" })
    val entry = RelayDatasetStatusRecord(
        status = current.result.status,
        message = current.result.message,
        datasetId = current.dataset.datasetId,
        correlationId = normalize(current.result.correlationId),
    )
    deliverRelayDatasetStatus("schedule-query", entry, channel)
}

/** Where relayDatasetStatus hands each record. */
interface RelayDatasetStatusWriter {
    suspend fun write(rec: RelayDatasetStatusRecord)
}

/** What relayDatasetStatus keeps of each PeekDataset response. */
data class RelayDatasetStatusRecord(val status: String, val message: String, val datasetId: String, val correlationId: String)

/** Sends records to acme.dispatch.v1.DispatchOrderService. */
class RelayDatasetStatusDispatchOrder(private val stub: DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub) : RelayDatasetStatusWriter {
    override suspend fun write(rec: RelayDatasetStatusRecord) {
        RpcBoundary.run("acme.dispatch.v1.DispatchOrderService/GetDispatchOrder") { stub.getDispatchOrder(getDispatchOrderRequest { this.dispatchOrderId = rec.status }) }
    }
}

/** Writes records to acme.maintenance.v1.ServiceScheduleQueryService. */
class RelayDatasetStatusServiceScheduleQuery(private val stub: ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub) : RelayDatasetStatusWriter {
    override suspend fun write(rec: RelayDatasetStatusRecord) {
        val request = browseServiceSchedulesRequest {
            this.filter = BrowseServiceSchedulesRequestKt.filter { this.query = rec.message }
        }
        RpcBoundary.run("acme.maintenance.v1.ServiceScheduleQueryService/BrowseServiceSchedules") { stub.browseServiceSchedules(request) }
    }
}

/** Writes records to acme.authorization.v1.AccessPolicyValidationService. */
class RelayDatasetStatusAccessPolicyValidation(private val stub: AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub) : RelayDatasetStatusWriter {
    override suspend fun write(rec: RelayDatasetStatusRecord) {
        val request = checkAccessPolicyEligibilityRequest {
            this.reason = rec.status
            this.accessPolicy = accessPolicy { this.name = rec.datasetId }
            this.details = CheckAccessPolicyEligibilityRequestKt.details { this.locale = rec.correlationId }
        }
        RpcBoundary.run("acme.authorization.v1.AccessPolicyValidationService/CheckAccessPolicyEligibility") { stub.checkAccessPolicyEligibility(request) }
    }
}

/** Passes records to acme.fuel.v1.FuelTransactionSyncService. */
class RelayDatasetStatusFuelTransactionSync(private val stub: FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub) : RelayDatasetStatusWriter {
    override suspend fun write(rec: RelayDatasetStatusRecord) {
        val request = syncFuelTransactionsRequest {
            this.details = SyncFuelTransactionsRequestKt.details { this.cursor = rec.correlationId; this.resolution = rec.message }
            this.fuelTransaction = fuelTransaction { this.source = rec.status }
        }
        stub.syncFuelTransactions(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class RelayDatasetStatusLogOnly : RelayDatasetStatusWriter {
    override suspend fun write(rec: RelayDatasetStatusRecord) {
        LoggerFactory.getLogger("RelayDatasetStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverRelayDatasetStatus(kind: String, rec: RelayDatasetStatusRecord, channel: ManagedChannel) {
    var sink: RelayDatasetStatusWriter = RelayDatasetStatusLogOnly()
    when (kind) {
        "dispatch-order" -> sink = RelayDatasetStatusDispatchOrder(DispatchOrderServiceGrpcKt.DispatchOrderServiceCoroutineStub(channel))
        "schedule-query" -> sink = RelayDatasetStatusServiceScheduleQuery(ServiceScheduleQueryServiceGrpcKt.ServiceScheduleQueryServiceCoroutineStub(channel))
        "access-policy-validation" -> sink = RelayDatasetStatusAccessPolicyValidation(AccessPolicyValidationServiceGrpcKt.AccessPolicyValidationServiceCoroutineStub(channel))
        "fuel-transaction-sync" -> sink = RelayDatasetStatusFuelTransactionSync(FuelTransactionSyncServiceGrpcKt.FuelTransactionSyncServiceCoroutineStub(channel))
    }
    sink.write(rec)
}
