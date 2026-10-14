package com.acme.transport.ops.linehaulimporter

import com.acme.configuration.v1.ConfigSetCalculationServiceGrpcKt
import com.acme.configuration.v1.getConfigSetRatesRequest
import com.acme.fuel.v1.FuelPolicyExportServiceGrpcKt
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.getFuelPolicyLifecycleRequest
import com.acme.fuel.v1.retryFuelPolicyExportRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.fuel.v1.FuelPolicyLifecycleService/GetFuelPolicyLifecycle and hands what it keeps to the sink of its kind. */
suspend fun copyFuelPolicyLifecycleDescription(channel: ManagedChannel) {
    val upstream = FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)
    val got = upstream.getFuelPolicyLifecycle(getFuelPolicyLifecycleRequest { this.fuelPolicyId = "live-copy-fuel-policy-lifecycle-description" })
    val record = CopyFuelPolicyLifecycleDescriptionRecord(
        description = got.fuelPolicy.description,
        message = got.result.message,
        name = got.fuelPolicy.name,
        fuelPolicyId = got.fuelPolicy.fuelPolicyId,
    )
    deliverCopyFuelPolicyLifecycleDescription("config-set-calculation", record, channel)
}

/** Where copyFuelPolicyLifecycleDescription hands each record. */
interface CopyFuelPolicyLifecycleDescriptionSink {
    suspend fun relay(rec: CopyFuelPolicyLifecycleDescriptionRecord)
}

/** What copyFuelPolicyLifecycleDescription keeps of each GetFuelPolicyLifecycle response. */
data class CopyFuelPolicyLifecycleDescriptionRecord(val description: String, val message: String, val name: String, val fuelPolicyId: String)

/** Sends records to acme.configuration.v1.ConfigSetCalculationService. */
class CopyFuelPolicyLifecycleDescriptionConfigSetCalculation(private val stub: ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub) : CopyFuelPolicyLifecycleDescriptionSink {
    override suspend fun relay(rec: CopyFuelPolicyLifecycleDescriptionRecord) {
        stub.getConfigSetRates(getConfigSetRatesRequest { this.configSetId = rec.name })
    }
}

/** Hands records to acme.fuel.v1.FuelPolicyExportService. */
class CopyFuelPolicyLifecycleDescriptionFuelPolicyExport(private val stub: FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub) : CopyFuelPolicyLifecycleDescriptionSink {
    override suspend fun relay(rec: CopyFuelPolicyLifecycleDescriptionRecord) {
        val request = retryFuelPolicyExportRequest {
            this.fuelPolicy = fuelPolicy { this.name = rec.description }
            this.fuelPolicyId = rec.name
        }
        RpcBoundary.run("acme.fuel.v1.FuelPolicyExportService/RetryFuelPolicyExport") { stub.retryFuelPolicyExport(request) }
    }
}

/** Only logs, for kinds no service takes yet. */
class CopyFuelPolicyLifecycleDescriptionLogOnly : CopyFuelPolicyLifecycleDescriptionSink {
    override suspend fun relay(rec: CopyFuelPolicyLifecycleDescriptionRecord) {
        LoggerFactory.getLogger("CopyFuelPolicyLifecycleDescriptionRecord").atInfo().addKeyValue("description", rec.description).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverCopyFuelPolicyLifecycleDescription(kind: String, rec: CopyFuelPolicyLifecycleDescriptionRecord, channel: ManagedChannel) {
    var sink: CopyFuelPolicyLifecycleDescriptionSink = CopyFuelPolicyLifecycleDescriptionLogOnly()
    when (kind) {
        "config-set-calculation" -> sink = CopyFuelPolicyLifecycleDescriptionConfigSetCalculation(ConfigSetCalculationServiceGrpcKt.ConfigSetCalculationServiceCoroutineStub(channel))
        "fuel-policy-export" -> sink = CopyFuelPolicyLifecycleDescriptionFuelPolicyExport(FuelPolicyExportServiceGrpcKt.FuelPolicyExportServiceCoroutineStub(channel))
    }
    sink.relay(rec)
}
