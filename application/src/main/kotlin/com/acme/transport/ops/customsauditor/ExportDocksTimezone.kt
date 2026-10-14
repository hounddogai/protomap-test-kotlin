package com.acme.transport.ops.customsauditor

import com.acme.common.v1.pageRequest
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.SuggestManagedDevicesRequestKt
import com.acme.devices.v1.suggestManagedDevicesRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.getFuelPolicyRatesRequest
import com.acme.sponsorships.v1.SponsorCalculationServiceGrpcKt
import com.acme.sponsorships.v1.calculateSponsorRequest
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.YardQueryServiceGrpcKt
import com.acme.yards.v1.findYardsRequest
import com.acme.yards.v1.summarizeDocksRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.yards.v1.DockQueryService/SummarizeDocks and hands what it keeps to the sink of its kind. */
suspend fun exportDocksTimezone(channel: ManagedChannel) {
    val reader = DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel)
    val found = reader.summarizeDocks(summarizeDocksRequest { this.dockId = "nightly-export-docks-timezone" })
    val row = ExportDocksTimezoneRecord(
        timezone = found.period.timezone,
        correlationId = found.result.correlationId,
    )
    dispatchExportDocksTimezone("yard-query", row, channel)
}

/** Where exportDocksTimezone hands each record. */
interface ExportDocksTimezoneSink {
    suspend fun write(rec: ExportDocksTimezoneRecord)
}

/** What exportDocksTimezone keeps of each SummarizeDocks response. */
data class ExportDocksTimezoneRecord(val timezone: String, val correlationId: String)

/** Hands records to acme.sponsorships.v1.SponsorCalculationService. */
class ExportDocksTimezoneSponsorCalculation(private val stub: SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub) : ExportDocksTimezoneSink {
    override suspend fun write(rec: ExportDocksTimezoneRecord) {
        stub.calculateSponsor(calculateSponsorRequest { this.reason = rec.correlationId; this.sponsorId = rec.timezone })
    }
}

/** Writes records to acme.yards.v1.YardQueryService. */
class ExportDocksTimezoneYardQuery(private val stub: YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub) : ExportDocksTimezoneSink {
    override suspend fun write(rec: ExportDocksTimezoneRecord) {
        stub.findYards(findYardsRequest { this.page = pageRequest { this.pageToken = rec.correlationId } })
    }
}

/** Hands records to acme.devices.v1.ManagedDeviceQueryService. */
class ExportDocksTimezoneManagedDeviceQuery(private val stub: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub) : ExportDocksTimezoneSink {
    override suspend fun write(rec: ExportDocksTimezoneRecord) {
        val request = suggestManagedDevicesRequest {
            this.filter = SuggestManagedDevicesRequestKt.filter { this.region = rec.correlationId }
        }
        stub.suggestManagedDevices(request)
    }
}

/** Passes records to acme.fuel.v1.FuelPolicyCalculationService. */
class ExportDocksTimezoneFuelPolicyCalculation(private val stub: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub) : ExportDocksTimezoneSink {
    override suspend fun write(rec: ExportDocksTimezoneRecord) {
        stub.getFuelPolicyRates(getFuelPolicyRatesRequest { this.fuelPolicyId = rec.correlationId })
    }
}

/** Only logs, for kinds no service takes yet. */
class ExportDocksTimezoneLogOnly : ExportDocksTimezoneSink {
    override suspend fun write(rec: ExportDocksTimezoneRecord) {
        LoggerFactory.getLogger("ExportDocksTimezoneRecord").atInfo().addKeyValue("timezone", rec.timezone).log("No sink for record")
    }
}

/** Picks the writer of a kind and runs it. */
suspend fun dispatchExportDocksTimezone(kind: String, rec: ExportDocksTimezoneRecord, channel: ManagedChannel) {
    var write: suspend (ExportDocksTimezoneRecord) -> Unit = ExportDocksTimezoneLogOnly()::write
    when (kind) {
        "sponsor-calculation" -> write = ExportDocksTimezoneSponsorCalculation(SponsorCalculationServiceGrpcKt.SponsorCalculationServiceCoroutineStub(channel))::write
        "yard-query" -> write = ExportDocksTimezoneYardQuery(YardQueryServiceGrpcKt.YardQueryServiceCoroutineStub(channel))::write
        "managed-device-query" -> write = ExportDocksTimezoneManagedDeviceQuery(ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(channel))::write
        "fuel-policy-calculation" -> write = ExportDocksTimezoneFuelPolicyCalculation(FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel))::write
    }
    write(rec)
}
