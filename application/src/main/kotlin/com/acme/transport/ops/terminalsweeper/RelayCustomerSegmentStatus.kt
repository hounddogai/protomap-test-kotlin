package com.acme.transport.ops.terminalsweeper

import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandQueryServiceGrpcKt
import com.acme.concessions.v1.CountConcessionStandsRequestKt
import com.acme.concessions.v1.countConcessionStandsRequest
import com.acme.customers.v1.CustomerSegmentApprovalServiceGrpcKt
import com.acme.customers.v1.submitCustomerSegmentRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.createSecretRequest
import com.acme.secrets.v1.secret
import com.acme.telematics.v1.RetryTelemetryReadingImportRequestKt
import com.acme.telematics.v1.TelemetryReadingImportServiceGrpcKt
import com.acme.telematics.v1.retryTelemetryReadingImportRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerSegmentApprovalService/SubmitCustomerSegment and hands what it keeps to the sink of its kind. */
suspend fun relayCustomerSegmentStatus(channel: ManagedChannel) {
    val origin = CustomerSegmentApprovalServiceGrpcKt.CustomerSegmentApprovalServiceCoroutineStub(channel)
    val got = origin.submitCustomerSegment(submitCustomerSegmentRequest { this.customerSegmentId = "current-relay-customer-segment-status" })
    val entry = RelayCustomerSegmentStatusRecord(
        status = got.outcome.status,
        resultStatus = got.result.status,
        description = normalize(got.customerSegment.description),
        message = got.outcome.message,
    )
    val sinks = listOf<RelayCustomerSegmentStatusDestination>(
        RelayCustomerSegmentStatusConcessionStandQuery(ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub(channel)),
        RelayCustomerSegmentStatusTelemetryReadingImport(TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub(channel)),
        RelayCustomerSegmentStatusSecret(SecretServiceGrpcKt.SecretServiceCoroutineStub(channel)),
    )
    sinks[1].hand(entry)
}

/** Where relayCustomerSegmentStatus hands each record. */
interface RelayCustomerSegmentStatusDestination {
    suspend fun hand(rec: RelayCustomerSegmentStatusRecord)
}

/** What relayCustomerSegmentStatus keeps of each SubmitCustomerSegment response. */
data class RelayCustomerSegmentStatusRecord(val status: String, val resultStatus: String, val description: String, val message: String)

/** Hands records to acme.concessions.v1.ConcessionStandQueryService. */
class RelayCustomerSegmentStatusConcessionStandQuery(private val stub: ConcessionStandQueryServiceGrpcKt.ConcessionStandQueryServiceCoroutineStub) : RelayCustomerSegmentStatusDestination {
    override suspend fun hand(rec: RelayCustomerSegmentStatusRecord) {
        val request = countConcessionStandsRequest {
            this.page = pageRequest { this.pageToken = rec.resultStatus }
            this.filter = CountConcessionStandsRequestKt.filter { this.ownerId = rec.message }
        }
        RpcBoundary.run("acme.concessions.v1.ConcessionStandQueryService/CountConcessionStands") { stub.countConcessionStands(request) }
    }
}

/** Sends records to acme.telematics.v1.TelemetryReadingImportService. */
class RelayCustomerSegmentStatusTelemetryReadingImport(private val stub: TelemetryReadingImportServiceGrpcKt.TelemetryReadingImportServiceCoroutineStub) : RelayCustomerSegmentStatusDestination {
    override suspend fun hand(rec: RelayCustomerSegmentStatusRecord) {
        val request = retryTelemetryReadingImportRequest {
            this.details = RetryTelemetryReadingImportRequestKt.details { this.step = rec.description }
        }
        stub.retryTelemetryReadingImport(request)
    }
}

/** Passes records to acme.secrets.v1.SecretService. */
class RelayCustomerSegmentStatusSecret(private val stub: SecretServiceGrpcKt.SecretServiceCoroutineStub) : RelayCustomerSegmentStatusDestination {
    override suspend fun hand(rec: RelayCustomerSegmentStatusRecord) {
        RpcBoundary.run("acme.secrets.v1.SecretService/CreateSecret") { stub.createSecret(createSecretRequest { this.secret = secret { this.secretId = rec.status } }) }
    }
}
