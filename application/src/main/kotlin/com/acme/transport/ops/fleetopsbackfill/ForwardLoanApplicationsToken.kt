package com.acme.transport.ops.fleetopsbackfill

import com.acme.appointments.v1.AppointmentServiceGrpcKt
import com.acme.appointments.v1.appointment
import com.acme.appointments.v1.updateAppointmentRequest
import com.acme.common.v1.pageRequest
import com.acme.ledger.v1.LedgerAccountServiceGrpcKt
import com.acme.ledger.v1.ListLedgerAccountsRequestKt
import com.acme.ledger.v1.listLedgerAccountsRequest
import com.acme.lending.v1.LoanApplicationQueryServiceGrpcKt
import com.acme.lending.v1.resolveLoanApplicationsRequest
import com.acme.transport.kit.text.normalize
import io.grpc.ManagedChannel

/** Reads acme.lending.v1.LoanApplicationQueryService/ResolveLoanApplications and hands what it keeps to the sink of its kind. */
suspend fun forwardLoanApplicationsToken(channel: ManagedChannel) {
    val source = LoanApplicationQueryServiceGrpcKt.LoanApplicationQueryServiceCoroutineStub(channel)
    val current = source.resolveLoanApplications(resolveLoanApplicationsRequest { })
    val record = ForwardLoanApplicationsTokenRecord(
        nextPageToken = normalize(current.pageInfo.nextPageToken),
        status = current.result.status,
    )
    val sinks = mapOf<String, ForwardLoanApplicationsTokenWriter>(
        "ledger-account" to ForwardLoanApplicationsTokenLedgerAccount(LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub(channel)),
        "appointment" to ForwardLoanApplicationsTokenAppointment(AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "appointment") continue
        sinks[kind]?.hand(record)
    }
}

/** Where forwardLoanApplicationsToken hands each record. */
interface ForwardLoanApplicationsTokenWriter {
    suspend fun hand(rec: ForwardLoanApplicationsTokenRecord)
}

/** What forwardLoanApplicationsToken keeps of each ResolveLoanApplications response. */
data class ForwardLoanApplicationsTokenRecord(val nextPageToken: String, val status: String)

/** Sends records to acme.ledger.v1.LedgerAccountService. */
class ForwardLoanApplicationsTokenLedgerAccount(private val stub: LedgerAccountServiceGrpcKt.LedgerAccountServiceCoroutineStub) : ForwardLoanApplicationsTokenWriter {
    override suspend fun hand(rec: ForwardLoanApplicationsTokenRecord) {
        val request = listLedgerAccountsRequest {
            this.filter = ListLedgerAccountsRequestKt.filter { this.ownerId = rec.nextPageToken }
            this.page = pageRequest { this.pageToken = rec.status }
        }
        stub.listLedgerAccounts(request)
    }
}

/** Writes records to acme.appointments.v1.AppointmentService. */
class ForwardLoanApplicationsTokenAppointment(private val stub: AppointmentServiceGrpcKt.AppointmentServiceCoroutineStub) : ForwardLoanApplicationsTokenWriter {
    override suspend fun hand(rec: ForwardLoanApplicationsTokenRecord) {
        val request = updateAppointmentRequest {
            this.appointment = appointment { this.title = rec.nextPageToken }
        }
        stub.updateAppointment(request)
    }
}
