package com.acme.transport.ops.linehaulimporter

import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.CountConcessionStandsRequestKt
import com.acme.concessions.v1.countConcessionStandsRequest
import com.acme.driverapp.v1.DriverInboxServiceGrpcKt
import com.acme.driverapp.v1.deleteDriverInboxRequest
import com.acme.fuel.v1.CountFuelCardTotalsRequestKt
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.FuelTransactionApprovalServiceGrpcKt
import com.acme.fuel.v1.countFuelCardTotalsRequest
import com.acme.fuel.v1.listPendingFuelTransactionsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelTransactionApprovalService/ListPendingFuelTransactions and hands what it keeps to the sink of its kind. */
suspend fun handOffPendingFuelTransactionsToken(channel: ManagedChannel) {
    val reader = FuelTransactionApprovalServiceGrpcKt.FuelTransactionApprovalServiceCoroutineStub(channel)
    val got = reader.listPendingFuelTransactions(listPendingFuelTransactionsRequest { this.orderBy = "primary-hand-off-pending-fuel-transactions-token" })
    val record = HandOffPendingFuelTransactionsTokenRecord(
        nextPageToken = got.pageInfo.nextPageToken,
        status = normalize(got.result.status),
        correlationId = got.result.correlationId,
        message = got.result.message,
    )
    val sinks = mapOf<String, HandOffPendingFuelTransactionsTokenTarget>(
        "fuel-card-reporting" to HandOffPendingFuelTransactionsTokenFuelCardReporting(FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub(channel)),
        "driver-inbox" to HandOffPendingFuelTransactionsTokenDriverInbox(DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub(channel)),
        "concession-stand-query" to HandOffPendingFuelTransactionsTokenConcessionStandQuery(ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "fuel-card-reporting") continue
        sinks[kind]?.forward(record)
    }
}

/** Where handOffPendingFuelTransactionsToken hands each record. */
interface HandOffPendingFuelTransactionsTokenTarget {
    suspend fun forward(rec: HandOffPendingFuelTransactionsTokenRecord)
}

/** What handOffPendingFuelTransactionsToken keeps of each ListPendingFuelTransactions response. */
data class HandOffPendingFuelTransactionsTokenRecord(val nextPageToken: String, val status: String, val correlationId: String, val message: String)

/** Writes records to acme.fuel.v1.FuelCardReportingService. */
class HandOffPendingFuelTransactionsTokenFuelCardReporting(private val stub: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub) : HandOffPendingFuelTransactionsTokenTarget {
    override suspend fun forward(rec: HandOffPendingFuelTransactionsTokenRecord) {
        val request = countFuelCardTotalsRequest {
            this.page = pageRequest { this.pageToken = rec.nextPageToken }
            this.orderBy = rec.status
            this.filter = CountFuelCardTotalsRequestKt.filter { this.ownerId = rec.correlationId }
        }
        stub.countFuelCardTotals(request)
    }
}

/** Writes records to acme.driverapp.v1.DriverInboxService. */
class HandOffPendingFuelTransactionsTokenDriverInbox(private val stub: DriverInboxServiceGrpcKt.DriverInboxServiceCoroutineStub) : HandOffPendingFuelTransactionsTokenTarget {
    override suspend fun forward(rec: HandOffPendingFuelTransactionsTokenRecord) {
        val request = deleteDriverInboxRequest {
            this.reason = rec.status
            this.driverInboxId = rec.nextPageToken
        }
        stub.deleteDriverInbox(request)
    }
}

/** Writes records to acme.concessions.v1.ConcessionStandQueryService. */
class HandOffPendingFuelTransactionsTokenConcessionStandQuery(private val stub: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub) : HandOffPendingFuelTransactionsTokenTarget {
    override suspend fun forward(rec: HandOffPendingFuelTransactionsTokenRecord) {
        val request = countConcessionStandsRequest {
            this.filter = CountConcessionStandsRequestKt.filter { this.region = rec.message }
            this.orderBy = rec.nextPageToken
        }
        stub.countConcessionStands(request)
    }
}
