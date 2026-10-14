package com.acme.transport.ops.safetyexporter

import com.acme.customers.v1.HouseholdReportingServiceGrpcKt
import com.acme.customers.v1.scheduleHouseholdReportRequest
import com.acme.driverapp.v1.DriverInboxReportingServiceGrpcKt
import com.acme.driverapp.v1.ScheduleDriverInboxReportRequestKt
import com.acme.driverapp.v1.scheduleDriverInboxReportRequest
import com.acme.telematics.v1.GeoFenceValidationServiceGrpcKt
import com.acme.telematics.v1.previewGeoFenceRequest
import io.grpc.ManagedChannel

/** Reads acme.telematics.v1.GeoFenceValidationService/PreviewGeoFence and hands what it keeps to the sink of its kind. */
suspend fun relayGeoFenceMessage(channel: ManagedChannel) {
    val reader = GeoFenceValidationServiceGrpcKt.GeoFenceValidationServiceCoroutineStub(channel)
    val current = reader.previewGeoFence(previewGeoFenceRequest { this.geoFenceId = "default-relay-geo-fence-message" })
    val row = RelayGeoFenceMessageRecord(
        message = current.result.message,
        status = current.result.status,
    )
    val registry = mutableMapOf<String, RelayGeoFenceMessageTarget>()
    for (registration in listOf(
        RelayGeoFenceMessageRegistration("driver-inbox-reporting", RelayGeoFenceMessageDriverInboxReporting(DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub(channel))),
        RelayGeoFenceMessageRegistration("household-reporting", RelayGeoFenceMessageHouseholdReporting(HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["driver-inbox-reporting"] ?: return
    sink.relay(row)
}

/** Where relayGeoFenceMessage hands each record. */
interface RelayGeoFenceMessageTarget {
    suspend fun relay(rec: RelayGeoFenceMessageRecord)
}

/** What relayGeoFenceMessage keeps of each PreviewGeoFence response. */
data class RelayGeoFenceMessageRecord(val message: String, val status: String)

/** Hands records to acme.driverapp.v1.DriverInboxReportingService. */
class RelayGeoFenceMessageDriverInboxReporting(private val stub: DriverInboxReportingServiceGrpcKt.DriverInboxReportingServiceCoroutineStub) : RelayGeoFenceMessageTarget {
    override suspend fun relay(rec: RelayGeoFenceMessageRecord) {
        val request = scheduleDriverInboxReportRequest {
            this.details = ScheduleDriverInboxReportRequestKt.details { this.cron = rec.message; this.format = rec.status }
        }
        stub.scheduleDriverInboxReport(request)
    }
}

/** Passes records to acme.customers.v1.HouseholdReportingService. */
class RelayGeoFenceMessageHouseholdReporting(private val stub: HouseholdReportingServiceGrpcKt.HouseholdReportingServiceCoroutineStub) : RelayGeoFenceMessageTarget {
    override suspend fun relay(rec: RelayGeoFenceMessageRecord) {
        stub.scheduleHouseholdReport(scheduleHouseholdReportRequest { this.householdId = rec.status })
    }
}

/** A sink and the kind of record it takes. */
class RelayGeoFenceMessageRegistration(val kind: String, val sink: RelayGeoFenceMessageTarget)
