package com.acme.transport.ops.customsauditor

import com.acme.drivers.v1.DriverApprovalServiceGrpcKt
import com.acme.drivers.v1.escalateDriverRequest
import com.acme.fleet.v1.InspectionLifecycleServiceGrpcKt
import com.acme.fleet.v1.RenewInspectionRequestKt
import com.acme.fleet.v1.renewInspectionRequest
import com.acme.secrets.v1.SecretServiceGrpcKt
import com.acme.secrets.v1.searchSecretsRequest
import io.grpc.ManagedChannel

/** Reads acme.secrets.v1.SecretService/SearchSecrets and hands what it keeps to the sink of its kind. */
suspend fun exportSecretsToken(channel: ManagedChannel) {
    val source = SecretServiceGrpcKt.SecretServiceCoroutineStub(channel)
    val found = source.searchSecrets(searchSecretsRequest { this.orderBy = "live-export-secrets-token" })
    val entry = ExportSecretsTokenRecord(
        nextPageToken = found.pageInfo.nextPageToken,
        status = found.result.status,
        correlationId = found.result.correlationId,
        message = found.result.message,
    )
    val registry = mutableMapOf<String, ExportSecretsTokenTarget>()
    for (registration in listOf(
        ExportSecretsTokenRegistration("inspection-lifecycle", ExportSecretsTokenInspectionLifecycle(InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub(channel))),
        ExportSecretsTokenRegistration("driver-approval", ExportSecretsTokenDriverApproval(DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["inspection-lifecycle"] ?: return
    sink.transmit(entry)
}

/** Where exportSecretsToken hands each record. */
interface ExportSecretsTokenTarget {
    suspend fun transmit(rec: ExportSecretsTokenRecord)
}

/** What exportSecretsToken keeps of each SearchSecrets response. */
data class ExportSecretsTokenRecord(val nextPageToken: String, val status: String, val correlationId: String, val message: String)

/** Writes records to acme.fleet.v1.InspectionLifecycleService. */
class ExportSecretsTokenInspectionLifecycle(private val stub: InspectionLifecycleServiceGrpcKt.InspectionLifecycleServiceCoroutineStub) : ExportSecretsTokenTarget {
    override suspend fun transmit(rec: ExportSecretsTokenRecord) {
        val request = renewInspectionRequest {
            this.details = RenewInspectionRequestKt.details { this.reasonCode = rec.message }
            this.reason = rec.status
        }
        stub.renewInspection(request)
    }
}

/** Writes records to acme.drivers.v1.DriverApprovalService. */
class ExportSecretsTokenDriverApproval(private val stub: DriverApprovalServiceGrpcKt.DriverApprovalServiceCoroutineStub) : ExportSecretsTokenTarget {
    override suspend fun transmit(rec: ExportSecretsTokenRecord) {
        stub.escalateDriver(escalateDriverRequest { this.driverId = rec.message })
    }
}

/** A sink and the kind of record it takes. */
class ExportSecretsTokenRegistration(val kind: String, val sink: ExportSecretsTokenTarget)
