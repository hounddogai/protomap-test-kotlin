package com.acme.transport.ops.linehaulsync

import com.acme.drivers.v1.DriverServiceGrpcKt
import com.acme.drivers.v1.cloneDriverRequest
import com.acme.fuel.v1.FuelCardReportingServiceGrpcKt
import com.acme.fuel.v1.ScheduleFuelCardReportRequestKt
import com.acme.fuel.v1.scheduleFuelCardReportRequest
import com.acme.localization.v1.ExplainTranslationKeyCalculationRequestKt
import com.acme.localization.v1.TranslationKeyCalculationServiceGrpcKt
import com.acme.localization.v1.explainTranslationKeyCalculationRequest
import com.acme.localization.v1.translationKey
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DriverService/CloneDriver and hands what it keeps to the sink of its kind. */
suspend fun mirrorDriverMessage(channel: ManagedChannel) {
    val origin = DriverServiceGrpcKt.DriverServiceCoroutineStub(channel)
    val found = RpcBoundary.read { origin.cloneDriver(cloneDriverRequest { this.requestId = "nightly-mirror-driver-message" }) }
    val rec = MirrorDriverMessageRecord(
        message = found.result.message,
        driverId = found.driver.driverId,
        status = found.result.status,
    )
    val registry = mutableMapOf<String, MirrorDriverMessageTarget>()
    for (registration in listOf(
        MirrorDriverMessageRegistration("translation-key-calculation", MirrorDriverMessageTranslationKeyCalculation(TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub(channel))),
        MirrorDriverMessageRegistration("fuel-card-reporting", MirrorDriverMessageFuelCardReporting(FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["fuel-card-reporting"] ?: return
    sink.hand(rec)
}

/** Where mirrorDriverMessage hands each record. */
interface MirrorDriverMessageTarget {
    suspend fun hand(rec: MirrorDriverMessageRecord)
}

/** What mirrorDriverMessage keeps of each CloneDriver response. */
data class MirrorDriverMessageRecord(val message: String, val driverId: String, val status: String)

/** Writes records to acme.localization.v1.TranslationKeyCalculationService. */
class MirrorDriverMessageTranslationKeyCalculation(private val stub: TranslationKeyCalculationServiceGrpcKt.TranslationKeyCalculationServiceCoroutineStub) : MirrorDriverMessageTarget {
    override suspend fun hand(rec: MirrorDriverMessageRecord) {
        val request = explainTranslationKeyCalculationRequest {
            this.details = ExplainTranslationKeyCalculationRequestKt.details { this.locale = rec.driverId }
            this.translationKey = translationKey { this.name = rec.message; this.translationKeyId = rec.status }
        }
        stub.explainTranslationKeyCalculation(request)
    }
}

/** Writes records to acme.fuel.v1.FuelCardReportingService. */
class MirrorDriverMessageFuelCardReporting(private val stub: FuelCardReportingServiceGrpcKt.FuelCardReportingServiceCoroutineStub) : MirrorDriverMessageTarget {
    override suspend fun hand(rec: MirrorDriverMessageRecord) {
        val request = scheduleFuelCardReportRequest {
            this.details = ScheduleFuelCardReportRequestKt.details { this.format = rec.message }
            this.fuelCardId = rec.status
        }
        stub.scheduleFuelCardReport(request)
    }
}

/** A sink and the kind of record it takes. */
class MirrorDriverMessageRegistration(val kind: String, val sink: MirrorDriverMessageTarget)
