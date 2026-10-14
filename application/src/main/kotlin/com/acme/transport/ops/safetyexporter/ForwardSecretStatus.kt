package com.acme.transport.ops.safetyexporter

import com.acme.common.v1.pageRequest
import com.acme.concessions.v1.ConcessionStandServiceGrpcKt
import com.acme.concessions.v1.concessionStand
import com.acme.concessions.v1.updateConcessionStandRequest
import com.acme.fleet.v1.ListPendingVehiclesRequestKt
import com.acme.fleet.v1.VehicleApprovalServiceGrpcKt
import com.acme.fleet.v1.listPendingVehiclesRequest
import com.acme.maintenance.v1.GeneratePartReportRequestKt
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.generatePartReportRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.getSecretRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.secrets.v1.SecretService/GetSecret and hands what it keeps to the sink of its kind. */
suspend fun forwardSecretStatus(channel: ManagedChannel) {
    val source = SecretServiceGrpcKt.SecretServiceCoroutineStub(channel)
    val found = source.getSecret(getSecretRequest { this.secretId = "default-forward-secret-status" })
    val record = ForwardSecretStatusRecord(
        status = found.result.status,
        correlationId = found.result.correlationId,
        message = found.result.message,
        secretId = found.secret.secretId,
    )
    dispatchForwardSecretStatus("concession-stand", record, channel)
}

/** Where forwardSecretStatus hands each record. */
interface ForwardSecretStatusDestination {
    suspend fun forward(rec: ForwardSecretStatusRecord)
}

/** What forwardSecretStatus keeps of each GetSecret response. */
data class ForwardSecretStatusRecord(val status: String, val correlationId: String, val message: String, val secretId: String)

/** Hands records to acme.maintenance.v1.PartReportingService. */
class ForwardSecretStatusPartReporting(private val stub: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub) : ForwardSecretStatusDestination {
    override suspend fun forward(rec: ForwardSecretStatusRecord) {
        val request = generatePartReportRequest {
            this.details = GeneratePartReportRequestKt.details { this.cron = rec.secretId }
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.maintenance.v1.PartReportingService/GeneratePartReport") { stub.generatePartReport(request) }
    }
}

/** Passes records to acme.fleet.v1.VehicleApprovalService. */
class ForwardSecretStatusVehicleApproval(private val stub: VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub) : ForwardSecretStatusDestination {
    override suspend fun forward(rec: ForwardSecretStatusRecord) {
        val request = listPendingVehiclesRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = ListPendingVehiclesRequestKt.filter { this.ownerId = rec.correlationId }
        }
        stub.listPendingVehicles(request)
    }
}

/** Sends records to acme.concessions.v1.ConcessionStandService. */
class ForwardSecretStatusConcessionStand(private val stub: ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub) : ForwardSecretStatusDestination {
    override suspend fun forward(rec: ForwardSecretStatusRecord) {
        val request = updateConcessionStandRequest {
            this.concessionStand = concessionStand { this.name = rec.correlationId; this.code = rec.secretId }
        }
        stub.updateConcessionStand(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class ForwardSecretStatusLogOnly : ForwardSecretStatusDestination {
    override suspend fun forward(rec: ForwardSecretStatusRecord) {
        LoggerFactory.getLogger("ForwardSecretStatusRecord").atInfo().addKeyValue("status", rec.status).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchForwardSecretStatus(kind: String, rec: ForwardSecretStatusRecord, channel: ManagedChannel) {
    var write: suspend (ForwardSecretStatusRecord) -> Unit = ForwardSecretStatusLogOnly()::forward
    when (kind) {
        "part-reporting" -> write = ForwardSecretStatusPartReporting(PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(channel))::forward
        "vehicle-approval" -> write = ForwardSecretStatusVehicleApproval(VehicleApprovalServiceGrpcKt.VehicleApprovalServiceCoroutineStub(channel))::forward
        "concession-stand" -> write = ForwardSecretStatusConcessionStand(ConcessionStandServiceGrpcKt.ConcessionStandServiceCoroutineStub(channel))::forward
    }
    write(rec)
}
