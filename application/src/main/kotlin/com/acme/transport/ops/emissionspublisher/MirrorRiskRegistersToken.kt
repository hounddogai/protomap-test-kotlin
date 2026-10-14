package com.acme.transport.ops.emissionspublisher

import com.acme.common.v1.pageRequest
import com.acme.risk.v1.RiskRegisterQueryServiceGrpcKt
import com.acme.risk.v1.findRiskRegistersRequest
import com.acme.workplaces.v1.BuildingQueryServiceGrpcKt
import com.acme.workplaces.v1.SuggestBuildingsRequestKt
import com.acme.workplaces.v1.suggestBuildingsRequest
import io.grpc.ManagedChannel

/** Reads acme.risk.v1.RiskRegisterQueryService/FindRiskRegisters and hands what it keeps to acme.workplaces.v1.BuildingQueryService. */
suspend fun mirrorRiskRegistersToken(channel: ManagedChannel) {
    val origin = RiskRegisterQueryServiceGrpcKt.RiskRegisterQueryServiceCoroutineStub(channel)
    val current = origin.findRiskRegisters(findRiskRegistersRequest { this.orderBy = "main-mirror-risk-registers-token" })
    val entry = MirrorRiskRegistersTokenRecord(
        nextPageToken = current.pageInfo.nextPageToken,
        status = current.result.status,
        correlationId = current.result.correlationId,
        message = current.result.message,
    )
    MirrorRiskRegistersTokenBuildingQuery(BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub(channel)).transmit(entry)
}

/** What mirrorRiskRegistersToken keeps of each FindRiskRegisters response. */
data class MirrorRiskRegistersTokenRecord(val nextPageToken: String, val status: String, val correlationId: String, val message: String)

/** Writes records to acme.workplaces.v1.BuildingQueryService. */
class MirrorRiskRegistersTokenBuildingQuery(private val stub: BuildingQueryServiceGrpcKt.BuildingQueryServiceCoroutineStub) {
    suspend fun transmit(rec: MirrorRiskRegistersTokenRecord) {
        val request = suggestBuildingsRequest {
            this.filter = SuggestBuildingsRequestKt.filter { this.ownerId = rec.message }
            this.page = pageRequest { this.pageToken = rec.status }
            this.orderBy = rec.nextPageToken
        }
        stub.suggestBuildings(request)
    }
}
