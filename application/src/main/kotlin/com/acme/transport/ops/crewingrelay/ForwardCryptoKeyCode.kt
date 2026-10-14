package com.acme.transport.ops.crewingrelay

import com.acme.drivers.v1.DriverLicenseWorkflowServiceGrpcKt
import com.acme.drivers.v1.resumeDriverLicenseWorkflowRequest
import com.acme.fuel.v1.FuelCardApprovalServiceGrpcKt
import com.acme.fuel.v1.SubmitFuelCardRequestKt
import com.acme.fuel.v1.submitFuelCardRequest
import com.acme.keymanagement.v1.CryptoKeyServiceGrpcKt
import com.acme.keymanagement.v1.cloneCryptoKeyRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.warehousing.v1.WarehouseServiceGrpcKt
import com.acme.warehousing.v1.cloneWarehouseRequest
import com.acme.warehousing.v1.warehouse
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.keymanagement.v1.CryptoKeyService/CloneCryptoKey and hands what it keeps to the sink of its kind. */
suspend fun forwardCryptoKeyCode(channel: ManagedChannel) {
    val reader = CryptoKeyServiceGrpcKt.CryptoKeyServiceCoroutineStub(channel)
    val reply = reader.cloneCryptoKey(cloneCryptoKeyRequest { this.requestId = "current-forward-crypto-key-code" })
    val rec = ForwardCryptoKeyCodeRecord(
        currencyCode = reply.cryptoKey.currencyCode,
        correlationId = reply.result.correlationId,
        message = reply.result.message,
        cryptoKeyId = reply.cryptoKey.cryptoKeyId,
    )
    dispatchForwardCryptoKeyCode("fuel-card-approval", rec, channel)
}

/** Where forwardCryptoKeyCode hands each record. */
interface ForwardCryptoKeyCodeTarget {
    suspend fun deliver(rec: ForwardCryptoKeyCodeRecord)
}

/** What forwardCryptoKeyCode keeps of each CloneCryptoKey response. */
data class ForwardCryptoKeyCodeRecord(val currencyCode: String, val correlationId: String, val message: String, val cryptoKeyId: String)

/** Passes records to acme.fuel.v1.FuelCardApprovalService. */
class ForwardCryptoKeyCodeFuelCardApproval(private val stub: FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub) : ForwardCryptoKeyCodeTarget {
    override suspend fun deliver(rec: ForwardCryptoKeyCodeRecord) {
        val request = submitFuelCardRequest {
            this.fuelCardId = rec.message
            this.details = SubmitFuelCardRequestKt.details { this.comment = rec.currencyCode; this.approverEmail = rec.correlationId }
        }
        stub.submitFuelCard(request)
    }
}

/** Sends records to acme.warehousing.v1.WarehouseService. */
class ForwardCryptoKeyCodeWarehouse(private val stub: WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub) : ForwardCryptoKeyCodeTarget {
    override suspend fun deliver(rec: ForwardCryptoKeyCodeRecord) {
        RpcBoundary.run("acme.warehousing.v1.WarehouseService/CloneWarehouse") { stub.cloneWarehouse(cloneWarehouseRequest { this.warehouse = warehouse { this.code = rec.currencyCode } }) }
    }
}

/** Sends records to acme.drivers.v1.DriverLicenseWorkflowService. */
class ForwardCryptoKeyCodeDriverLicenseWorkflow(private val stub: DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub) : ForwardCryptoKeyCodeTarget {
    override suspend fun deliver(rec: ForwardCryptoKeyCodeRecord) {
        RpcBoundary.run("acme.drivers.v1.DriverLicenseWorkflowService/ResumeDriverLicenseWorkflow") { stub.resumeDriverLicenseWorkflow(resumeDriverLicenseWorkflowRequest { this.driverLicenseId = rec.correlationId }) }
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardCryptoKeyCodeLogOnly : ForwardCryptoKeyCodeTarget {
    override suspend fun deliver(rec: ForwardCryptoKeyCodeRecord) {
        LoggerFactory.getLogger("ForwardCryptoKeyCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchForwardCryptoKeyCode(kind: String, rec: ForwardCryptoKeyCodeRecord, channel: ManagedChannel) {
    var write: suspend (ForwardCryptoKeyCodeRecord) -> Unit = ForwardCryptoKeyCodeLogOnly()::deliver
    when (kind) {
        "fuel-card-approval" -> write = ForwardCryptoKeyCodeFuelCardApproval(FuelCardApprovalServiceGrpcKt.FuelCardApprovalServiceCoroutineStub(channel))::deliver
        "warehouse" -> write = ForwardCryptoKeyCodeWarehouse(WarehouseServiceGrpcKt.WarehouseServiceCoroutineStub(channel))::deliver
        "driver-license-workflow" -> write = ForwardCryptoKeyCodeDriverLicenseWorkflow(DriverLicenseWorkflowServiceGrpcKt.DriverLicenseWorkflowServiceCoroutineStub(channel))::deliver
    }
    write(rec)
}
