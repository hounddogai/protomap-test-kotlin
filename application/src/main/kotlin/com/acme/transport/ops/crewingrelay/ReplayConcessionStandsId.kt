package com.acme.transport.ops.crewingrelay

import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.countConcessionStandsRequest
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.ListDriverInboxReportsRequestKt
import com.acme.driverapp.v1.listDriverInboxReportsRequest
import io.grpc.ManagedChannel

/** Reads acme.concessions.v1.ConcessionStandQueryService/CountConcessionStands and hands what it keeps to acme.driverapp.v1.DriverInboxReportingService. */
suspend fun replayConcessionStandsId(channel: ManagedChannel) {
    val origin = ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)
    val found = origin.countConcessionStands(countConcessionStandsRequest { this.orderBy = "current-replay-concession-stands-id" })
    val entry = ReplayConcessionStandsIdRecord(
        correlationId = found.result.correlationId,
        message = found.result.message,
    )
    ReplayConcessionStandsIdDriverInboxReporting(DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(channel)).transmit(entry)
}

/** What replayConcessionStandsId keeps of each CountConcessionStands response. */
data class ReplayConcessionStandsIdRecord(val correlationId: String, val message: String)

/** Writes records to acme.driverapp.v1.DriverInboxReportingService. */
class ReplayConcessionStandsIdDriverInboxReporting(private val stub: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub) {
    suspend fun transmit(rec: ReplayConcessionStandsIdRecord) {
        val request = listDriverInboxReportsRequest {
            this.orderBy = rec.message
            this.filter = ListDriverInboxReportsRequestKt.filter { this.ownerId = rec.correlationId }
        }
        stub.listDriverInboxReports(request)
    }
}
