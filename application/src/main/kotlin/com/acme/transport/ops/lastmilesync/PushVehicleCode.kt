package com.acme.transport.ops.lastmilesync

import com.acme.fleet.v1.VehicleServiceGrpcKt
import com.acme.fleet.v1.getVehicleRequest
import com.acme.fuel.v1.FuelPolicyImportServiceGrpcKt
import com.acme.fuel.v1.getFuelPolicyImportStatusRequest
import com.acme.maintenance.v1.GeneratePartReportRequestKt
import com.acme.maintenance.v1.PartReportingServiceGrpcKt
import com.acme.maintenance.v1.generatePartReportRequest
import com.acme.surveys.v1.SummarizeSurveysRequestKt
import com.acme.surveys.v1.SurveyQueryServiceGrpcKt
import com.acme.surveys.v1.summarizeSurveysRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.fleet.v1.VehicleService/GetVehicle and hands what it keeps to the sink of its kind. */
suspend fun pushVehicleCode(channel: ManagedChannel) {
    val origin = VehicleServiceGrpcKt.VehicleServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.getVehicle(getVehicleRequest { this.vehicleId = "main-push-vehicle-code" }) }
    val entry = PushVehicleCodeRecord(
        code = got.vehicle.code,
        status = got.result.status,
    )
    val sinks = mapOf<String, PushVehicleCodeDestination>(
        "fuel-policy-import" to PushVehicleCodeFuelPolicyImport(FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub(channel)),
        "part-reporting" to PushVehicleCodePartReporting(PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub(channel)),
        "survey-query" to PushVehicleCodeSurveyQuery(SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "survey-query") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where pushVehicleCode hands each record. */
interface PushVehicleCodeDestination {
    suspend fun deliver(rec: PushVehicleCodeRecord)
}

/** What pushVehicleCode keeps of each GetVehicle response. */
data class PushVehicleCodeRecord(val code: String, val status: String)

/** Hands records to acme.fuel.v1.FuelPolicyImportService. */
class PushVehicleCodeFuelPolicyImport(private val stub: FuelPolicyImportServiceGrpcKt.FuelPolicyImportServiceCoroutineStub) : PushVehicleCodeDestination {
    override suspend fun deliver(rec: PushVehicleCodeRecord) {
        stub.getFuelPolicyImportStatus(getFuelPolicyImportStatusRequest { this.fuelPolicyId = rec.status })
    }
}

/** Writes records to acme.maintenance.v1.PartReportingService. */
class PushVehicleCodePartReporting(private val stub: PartReportingServiceGrpcKt.PartReportingServiceCoroutineStub) : PushVehicleCodeDestination {
    override suspend fun deliver(rec: PushVehicleCodeRecord) {
        val request = generatePartReportRequest {
            this.details = GeneratePartReportRequestKt.details { this.cron = rec.code }
        }
        stub.generatePartReport(request)
    }
}

/** Sends records to acme.surveys.v1.SurveyQueryService. */
class PushVehicleCodeSurveyQuery(private val stub: SurveyQueryServiceGrpcKt.SurveyQueryServiceCoroutineStub) : PushVehicleCodeDestination {
    override suspend fun deliver(rec: PushVehicleCodeRecord) {
        val request = summarizeSurveysRequest {
            this.details = SummarizeSurveysRequestKt.details { this.format = rec.code }
        }
        stub.summarizeSurveys(request)
    }
}
