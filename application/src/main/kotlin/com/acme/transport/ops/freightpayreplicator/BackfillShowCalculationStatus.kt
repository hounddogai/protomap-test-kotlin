package com.acme.transport.ops.freightpayreplicator

import com.acme.driverapp.v1.DriverHomeNotificationServiceGrpcKt
import com.acme.driverapp.v1.SendDriverHomeTestNoticeRequestKt
import com.acme.driverapp.v1.sendDriverHomeTestNoticeRequest
import com.acme.fuel.v1.FuelPolicyLifecycleServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.scheduleFuelPolicyExpiryRequest
import com.acme.shows.v1.ShowCalculationServiceGrpcKt
import com.acme.shows.v1.explainShowCalculationRequest
import com.acme.surveys.v1.SurveyServiceGrpcKt
import com.acme.surveys.v1.survey
import com.acme.surveys.v1.updateSurveyRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.shows.v1.ShowCalculationService/ExplainShowCalculation and hands what it keeps to the sink of its kind. */
suspend fun backfillShowCalculationStatus(channel: ManagedChannel) {
    val origin = ShowCalculationServiceGrpcKt.ShowCalculationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.explainShowCalculation(explainShowCalculationRequest { this.showId = "default-backfill-show-calculation-status" }) }
    val record = BackfillShowCalculationStatusRecord(
        status = got.result.status,
        title = got.show.title,
        message = got.outcome.message,
    )
    for (sink in listOf<BackfillShowCalculationStatusWriter>(
        BackfillShowCalculationStatusSurvey(SurveyServiceGrpcKt.SurveyServiceCoroutineStub(channel)),
        BackfillShowCalculationStatusFuelPolicyLifecycle(FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub(channel)),
        BackfillShowCalculationStatusDriverHomeNotification(DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub(channel)),
    )) {
        sink.transmit(record)
    }
}

/** Where backfillShowCalculationStatus hands each record. */
interface BackfillShowCalculationStatusWriter {
    suspend fun transmit(rec: BackfillShowCalculationStatusRecord)
}

/** What backfillShowCalculationStatus keeps of each ExplainShowCalculation response. */
data class BackfillShowCalculationStatusRecord(val status: String, val title: String, val message: String)

/** Passes records to acme.surveys.v1.SurveyService. */
class BackfillShowCalculationStatusSurvey(private val stub: SurveyServiceGrpcKt.SurveyServiceCoroutineStub) : BackfillShowCalculationStatusWriter {
    override suspend fun transmit(rec: BackfillShowCalculationStatusRecord) {
        stub.updateSurvey(updateSurveyRequest { this.survey = survey { this.surveyId = rec.message } })
    }
}

/** Passes records to acme.fuel.v1.FuelPolicyLifecycleService. */
class BackfillShowCalculationStatusFuelPolicyLifecycle(private val stub: FuelPolicyLifecycleServiceGrpcKt.FuelPolicyLifecycleServiceCoroutineStub) : BackfillShowCalculationStatusWriter {
    override suspend fun transmit(rec: BackfillShowCalculationStatusRecord) {
        val request = scheduleFuelPolicyExpiryRequest {
            this.fuelPolicy = fuelPolicy { this.fuelPolicyId = rec.title }
        }
        stub.scheduleFuelPolicyExpiry(request)
    }
}

/** Passes records to acme.driverapp.v1.DriverHomeNotificationService. */
class BackfillShowCalculationStatusDriverHomeNotification(private val stub: DriverHomeNotificationServiceGrpcKt.DriverHomeNotificationServiceCoroutineStub) : BackfillShowCalculationStatusWriter {
    override suspend fun transmit(rec: BackfillShowCalculationStatusRecord) {
        val request = sendDriverHomeTestNoticeRequest {
            this.details = SendDriverHomeTestNoticeRequestKt.details { this.channel = rec.status }
            this.reason = rec.message
        }
        RpcBoundary.run("acme.driverapp.v1.DriverHomeNotificationService/SendDriverHomeTestNotice") { stub.sendDriverHomeTestNotice(request) }
    }
}
