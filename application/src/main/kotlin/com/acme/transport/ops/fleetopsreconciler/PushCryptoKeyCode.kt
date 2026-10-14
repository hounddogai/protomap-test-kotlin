package com.acme.transport.ops.fleetopsreconciler

import com.acme.keymanagement.v1.CryptoKeyCalculationServiceGrpcKt
import com.acme.keymanagement.v1.calculateCryptoKeyRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockApprovalServiceGrpcKt
import com.acme.yards.v1.dock
import com.acme.yards.v1.rejectDockRequest
import io.grpc.ManagedChannel

/** Reads acme.keymanagement.v1.CryptoKeyCalculationService/CalculateCryptoKey and hands what it keeps to acme.yards.v1.DockApprovalService. */
suspend fun pushCryptoKeyCode(channel: ManagedChannel) {
    val origin = CryptoKeyCalculationServiceGrpcKt.CryptoKeyCalculationServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { origin.calculateCryptoKey(calculateCryptoKeyRequest { this.cryptoKeyId = "current-push-crypto-key-code" }) }
    val entry = PushCryptoKeyCodeRecord(
        currencyCode = reply.breakdown.currencyCode,
        cryptoKeyId = normalize(reply.cryptoKey.cryptoKeyId),
    )
    PushCryptoKeyCodeDockApproval(DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub(channel)).deliver(entry)
}

/** What pushCryptoKeyCode keeps of each CalculateCryptoKey response. */
data class PushCryptoKeyCodeRecord(val currencyCode: String, val cryptoKeyId: String)

/** Writes records to acme.yards.v1.DockApprovalService. */
class PushCryptoKeyCodeDockApproval(private val stub: DockApprovalServiceGrpcKt.DockApprovalServiceCoroutineStub) {
    suspend fun deliver(rec: PushCryptoKeyCodeRecord) {
        val request = rejectDockRequest {
            this.dock = dock { this.name = rec.currencyCode; this.code = rec.cryptoKeyId }
        }
        stub.rejectDock(request)
    }
}
