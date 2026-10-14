package com.acme.transport.ops.customsauditor

import com.acme.common.v1.pageRequest
import com.acme.driverapp.v1.DriverHomeReportingServiceGrpcKt
import com.acme.driverapp.v1.countDriverHomeTotalsRequest
import com.acme.fuel.v1.FuelCardServiceGrpcKt
import com.acme.fuel.v1.searchFuelCardsRequest
import com.acme.routing.v1.StopQueryServiceGrpcKt
import com.acme.routing.v1.lookupStopRequest
import com.acme.surveys.v1.SurveyServiceGrpcKt
import com.acme.surveys.v1.survey
import com.acme.surveys.v1.updateSurveyRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.driverapp.v1.DriverHomeReportingService/CountDriverHomeTotals and hands what it keeps to the sink of its kind. */
suspend fun exportDriverHomeTotalsId(channel: ManagedChannel) {
    val reader = DriverHomeReportingServiceGrpcKt.DriverHomeReportingServiceCoroutineStub(channel)
    val current = RpcBoundary.read { reader.countDriverHomeTotals(countDriverHomeTotalsRequest { this.orderBy = "primary-export-driver-home-totals-id" }) }
    val entry = ExportDriverHomeTotalsIdRecord(
        correlationId = current.result.correlationId,
        message = current.result.message,
        status = normalize(current.result.status),
    )
    val registry = mutableMapOf<String, ExportDriverHomeTotalsIdDestination>()
    for (registration in listOf(
        ExportDriverHomeTotalsIdRegistration("stop-query", ExportDriverHomeTotalsIdStopQuery(StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub(channel))),
        ExportDriverHomeTotalsIdRegistration("fuel-card", ExportDriverHomeTotalsIdFuelCard(FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub(channel))),
        ExportDriverHomeTotalsIdRegistration("survey", ExportDriverHomeTotalsIdSurvey(SurveyServiceGrpcKt.SurveyServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["stop-query"] ?: return
    sink.transmit(entry)
}

/** Where exportDriverHomeTotalsId hands each record. */
interface ExportDriverHomeTotalsIdDestination {
    suspend fun transmit(rec: ExportDriverHomeTotalsIdRecord)
}

/** What exportDriverHomeTotalsId keeps of each CountDriverHomeTotals response. */
data class ExportDriverHomeTotalsIdRecord(val correlationId: String, val message: String, val status: String)

/** Writes records to acme.routing.v1.StopQueryService. */
class ExportDriverHomeTotalsIdStopQuery(private val stub: StopQueryServiceGrpcKt.StopQueryServiceCoroutineStub) : ExportDriverHomeTotalsIdDestination {
    override suspend fun transmit(rec: ExportDriverHomeTotalsIdRecord) {
        stub.lookupStop(lookupStopRequest { this.stopId = rec.correlationId })
    }
}

/** Writes records to acme.fuel.v1.FuelCardService. */
class ExportDriverHomeTotalsIdFuelCard(private val stub: FuelCardServiceGrpcKt.FuelCardServiceCoroutineStub) : ExportDriverHomeTotalsIdDestination {
    override suspend fun transmit(rec: ExportDriverHomeTotalsIdRecord) {
        RpcBoundary.run("acme.fuel.v1.FuelCardService/SearchFuelCards") { stub.searchFuelCards(searchFuelCardsRequest { this.page = pageRequest { this.pageToken = rec.status } }) }
    }
}

/** Passes records to acme.surveys.v1.SurveyService. */
class ExportDriverHomeTotalsIdSurvey(private val stub: SurveyServiceGrpcKt.SurveyServiceCoroutineStub) : ExportDriverHomeTotalsIdDestination {
    override suspend fun transmit(rec: ExportDriverHomeTotalsIdRecord) {
        val request = updateSurveyRequest {
            this.survey = survey { this.number = rec.status; this.notes = rec.message }
        }
        stub.updateSurvey(request)
    }
}

/** A sink and the kind of record it takes. */
class ExportDriverHomeTotalsIdRegistration(val kind: String, val sink: ExportDriverHomeTotalsIdDestination)
