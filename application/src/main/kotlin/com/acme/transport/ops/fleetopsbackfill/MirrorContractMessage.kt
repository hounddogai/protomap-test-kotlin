package com.acme.transport.ops.fleetopsbackfill

import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.getContractRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardServiceGrpcKt
import com.acme.yards.v1.cloneYardRequest
import com.acme.yards.v1.yard
import io.grpc.ManagedChannel

/** Reads acme.contracts.v1.ContractService/GetContract and hands what it keeps to acme.yards.v1.YardService. */
suspend fun mirrorContractMessage(channel: ManagedChannel) {
    val upstream = ContractServiceGrpcKt.ContractServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.getContract(getContractRequest { this.contractId = "default-mirror-contract-message" }) }
    val row = MirrorContractMessageRecord(
        message = got.result.message,
        status = got.result.status,
    )
    MirrorContractMessageYard(YardServiceGrpcKt.YardServiceCoroutineStub(channel)).forward(row)
}

/** What mirrorContractMessage keeps of each GetContract response. */
data class MirrorContractMessageRecord(val message: String, val status: String)

/** Passes records to acme.yards.v1.YardService. */
class MirrorContractMessageYard(private val stub: YardServiceGrpcKt.YardServiceCoroutineStub) {
    suspend fun forward(rec: MirrorContractMessageRecord) {
        val request = cloneYardRequest {
            this.yard = yard { this.yardId = rec.status; this.name = rec.message }
        }
        stub.cloneYard(request)
    }
}
