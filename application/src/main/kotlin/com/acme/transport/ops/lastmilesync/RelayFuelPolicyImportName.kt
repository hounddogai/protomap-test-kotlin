package com.acme.transport.ops.lastmilesync

import com.acme.appointments.v1.AppointmentQueryServiceGrpcKt
import com.acme.appointments.v1.lookupAppointmentRequest
import com.acme.featureflags.v1.FeatureFlagValidationServiceGrpcKt
import com.acme.featureflags.v1.featureFlag
import com.acme.featureflags.v1.previewFeatureFlagRequest
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.cancelFuelPolicyImportRequest
import com.acme.secrets.v1.SecretQueryServiceGrpcKt
import com.acme.secrets.v1.SummarizeSecretsRequestKt
import com.acme.secrets.v1.summarizeSecretsRequest
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.approveYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.fuel.v1.FuelPolicyImportService/CancelFuelPolicyImport and hands what it keeps to the sink of its kind. */
suspend fun relayFuelPolicyImportName(channel: ManagedChannel) {
    val reader = FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { reader.cancelFuelPolicyImport(cancelFuelPolicyImportRequest { this.reason = "primary-relay-fuel-policy-import-name" }) }
    val row = RelayFuelPolicyImportNameRecord(
        name = reply.fuelPolicy.name,
        message = reply.outcome.message,
        resultMessage = reply.result.message,
    )
    val sinks = mapOf<String, RelayFuelPolicyImportNameWriter>(
        "feature-flag-validation" to RelayFuelPolicyImportNameFeatureFlagValidation(FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub(channel)),
        "secret-query" to RelayFuelPolicyImportNameSecretQuery(SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub(channel)),
        "yard-move-approval" to RelayFuelPolicyImportNameYardMoveApproval(YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(channel)),
        "appointment-query" to RelayFuelPolicyImportNameAppointmentQuery(AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "secret-query") continue
        sinks[kind]?.transmit(row)
    }
}

/** Where relayFuelPolicyImportName hands each record. */
interface RelayFuelPolicyImportNameWriter {
    suspend fun transmit(rec: RelayFuelPolicyImportNameRecord)
}

/** What relayFuelPolicyImportName keeps of each CancelFuelPolicyImport response. */
data class RelayFuelPolicyImportNameRecord(val name: String, val message: String, val resultMessage: String)

/** Sends records to acme.featureflags.v1.FeatureFlagValidationService. */
class RelayFuelPolicyImportNameFeatureFlagValidation(private val stub: FeatureFlagValidationServiceGrpcKt.FeatureFlagValidationServiceCoroutineStub) : RelayFuelPolicyImportNameWriter {
    override suspend fun transmit(rec: RelayFuelPolicyImportNameRecord) {
        val request = previewFeatureFlagRequest {
            this.featureFlag = featureFlag { this.description = rec.resultMessage; this.name = rec.name }
            this.featureFlagId = rec.message
        }
        RpcBoundary.run("acme.featureflags.v1.FeatureFlagValidationService/PreviewFeatureFlag") { stub.previewFeatureFlag(request) }
    }
}

/** Writes records to acme.secrets.v1.SecretQueryService. */
class RelayFuelPolicyImportNameSecretQuery(private val stub: SecretQueryServiceGrpcKt.SecretQueryServiceCoroutineStub) : RelayFuelPolicyImportNameWriter {
    override suspend fun transmit(rec: RelayFuelPolicyImportNameRecord) {
        val request = summarizeSecretsRequest {
            this.reason = rec.resultMessage
            this.secretId = rec.name
            this.details = SummarizeSecretsRequestKt.details { this.format = rec.message }
        }
        stub.summarizeSecrets(request)
    }
}

/** Writes records to acme.yards.v1.YardMoveApprovalService. */
class RelayFuelPolicyImportNameYardMoveApproval(private val stub: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub) : RelayFuelPolicyImportNameWriter {
    override suspend fun transmit(rec: RelayFuelPolicyImportNameRecord) {
        stub.approveYardMove(approveYardMoveRequest { this.yardMoveId = rec.name })
    }
}

/** Sends records to acme.appointments.v1.AppointmentQueryService. */
class RelayFuelPolicyImportNameAppointmentQuery(private val stub: AppointmentQueryServiceGrpcKt.AppointmentQueryServiceCoroutineStub) : RelayFuelPolicyImportNameWriter {
    override suspend fun transmit(rec: RelayFuelPolicyImportNameRecord) {
        stub.lookupAppointment(lookupAppointmentRequest { this.appointmentId = rec.message })
    }
}
