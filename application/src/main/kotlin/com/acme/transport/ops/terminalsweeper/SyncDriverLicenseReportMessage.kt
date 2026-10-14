package com.acme.transport.ops.terminalsweeper

import com.acme.drivers.v1.DriverLicenseLifecycleServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseReportingServiceGrpcKt
import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.ReinstateDriverLicenseRequestKt
import com.acme.drivers.v1.driverLicense
import com.acme.drivers.v1.fetchDriverLicenseReportRequest
import com.acme.drivers.v1.reinstateDriverLicenseRequest
import com.acme.drivers.v1.retryDriverLicenseWorkflowRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.delegateFuelCardApprovalRequest
import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.RecalculateCryptoKeysRequestKt
import com.acme.keymanagement.v1.recalculateCryptoKeysRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.drivers.v1.DriverLicenseReportingService/FetchDriverLicenseReport and hands what it keeps to the sink of its kind. */
suspend fun syncDriverLicenseReportMessage(channel: ManagedChannel) {
    val origin = DriverLicenseReportingServiceGrpcKt.DriverLicenseReportingServiceCoroutineStub(channel)
    val current = RpcBoundary.read { origin.fetchDriverLicenseReport(fetchDriverLicenseReportRequest { this.driverLicenseId = "default-sync-driver-license-report-message" }) }
    val record = SyncDriverLicenseReportMessageRecord(
        message = normalize(current.result.message),
        driverLicenseId = normalize(current.driverLicense.driverLicenseId),
    )
    dispatchSyncDriverLicenseReportMessage("crypto-key-calculation", record, channel)
}

/** Where syncDriverLicenseReportMessage hands each record. */
interface SyncDriverLicenseReportMessageSink {
    suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord)
}

/** What syncDriverLicenseReportMessage keeps of each FetchDriverLicenseReport response. */
data class SyncDriverLicenseReportMessageRecord(val message: String, val driverLicenseId: String)

/** Passes records to acme.drivers.v1.DriverLicenseWorkflowService. */
class SyncDriverLicenseReportMessageDriverLicenseWorkflow(private val stub: DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub) : SyncDriverLicenseReportMessageSink {
    override suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord) {
        val request = retryDriverLicenseWorkflowRequest {
            this.driverLicense = driverLicense { this.driverLicenseId = rec.message }
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/RetryDriverLicenseWorkflow") { stub.retryDriverLicenseWorkflow(request) }
    }
}

/** Hands records to acme.keymanagement.v1.CryptoKeyCalculationService. */
class SyncDriverLicenseReportMessageCryptoKeyCalculation(private val stub: CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub) : SyncDriverLicenseReportMessageSink {
    override suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord) {
        val request = recalculateCryptoKeysRequest {
            this.details = RecalculateCryptoKeysRequestKt.details { this.currencyCode = rec.message }
        }
        stub.recalculateCryptoKeys(request)
    }
}

/** Passes records to acme.fuel.v1.FuelCardApprovalService. */
class SyncDriverLicenseReportMessageFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : SyncDriverLicenseReportMessageSink {
    override suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord) {
        stub.delegateFuelCardApproval(delegateFuelCardApprovalRequest { this.fuelCardId = rec.message })
    }
}

/** Passes records to acme.drivers.v1.DriverLicenseLifecycleService. */
class SyncDriverLicenseReportMessageDriverLicenseLifecycle(private val stub: DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub) : SyncDriverLicenseReportMessageSink {
    override suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord) {
        val request = reinstateDriverLicenseRequest {
            this.details = ReinstateDriverLicenseRequestKt.details { this.reasonCode = rec.message }
            this.driverLicenseId = rec.driverLicenseId
        }
        RpcBoundary.run("acme.drivers.v1.DriverLicenseLifecycleService/ReinstateDriverLicense") { stub.reinstateDriverLicense(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class SyncDriverLicenseReportMessageLogOnly : SyncDriverLicenseReportMessageSink {
    override suspend fun deliver(rec: SyncDriverLicenseReportMessageRecord) {
        LoggerFactory.getLogger("SyncDriverLicenseReportMessageRecord").atInfo().addKeyValue("message", rec.message).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchSyncDriverLicenseReportMessage(kind: String, rec: SyncDriverLicenseReportMessageRecord, channel: ManagedChannel) {
    var write: suspend (SyncDriverLicenseReportMessageRecord) -> Unit = SyncDriverLicenseReportMessageLogOnly()::deliver
    when (kind) {
        "driver-license-workflow" -> write = SyncDriverLicenseReportMessageDriverLicenseWorkflow(DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub(channel))::deliver
        "crypto-key-calculation" -> write = SyncDriverLicenseReportMessageCryptoKeyCalculation(CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(channel))::deliver
        "fuel-card-approval" -> write = SyncDriverLicenseReportMessageFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel))::deliver
        "driver-license-lifecycle" -> write = SyncDriverLicenseReportMessageDriverLicenseLifecycle(DriverLicenseLifecycleServiceGrpcKt.DriverLicenseLifecycleServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
