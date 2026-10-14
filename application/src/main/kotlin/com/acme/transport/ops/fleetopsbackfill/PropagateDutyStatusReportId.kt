package com.acme.transport.ops.fleetopsbackfill

import com.acme.common.v1.pageRequest
import com.acme.devices.v1.ManagedDeviceQueryServiceGrpcKt
import com.acme.devices.v1.SuggestManagedDevicesRequestKt
import com.acme.devices.v1.suggestManagedDevicesRequest
import com.acme.drivers.v1.DutyStatusReportingServiceGrpcKt
import com.acme.drivers.v1.scheduleDutyStatusReportRequest
import com.acme.telematics.v1.TelemetryReadingServiceGrpcKt
import com.acme.telematics.v1.cloneTelemetryReadingRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.drivers.v1.DutyStatusReportingService/ScheduleDutyStatusReport and hands what it keeps to the sink of its kind. */
suspend fun propagateDutyStatusReportId(channel: ManagedChannel) {
    val origin = DutyStatusReportingServiceGrpcKt.DutyStatusReportingServiceCoroutineStub(channel)
    val current = origin.scheduleDutyStatusReport(scheduleDutyStatusReportRequest { this.dutyStatusId = "main-propagate-duty-status-report-id" })
    val entry = PropagateDutyStatusReportIdRecord(
        reportId = current.reportId,
        timezone = current.period.timezone,
    )
    val sinks = mapOf<String, PropagateDutyStatusReportIdTarget>(
        "telemetry-reading" to PropagateDutyStatusReportIdTelemetryReading(TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub(channel)),
        "managed-device-query" to PropagateDutyStatusReportIdManagedDeviceQuery(ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub(channel)),
    )
    for (kind in sinks.keys) {
        if (kind != "telemetry-reading") continue
        sinks[kind]?.deliver(entry)
    }
}

/** Where propagateDutyStatusReportId hands each record. */
interface PropagateDutyStatusReportIdTarget {
    suspend fun deliver(rec: PropagateDutyStatusReportIdRecord)
}

/** What propagateDutyStatusReportId keeps of each ScheduleDutyStatusReport response. */
data class PropagateDutyStatusReportIdRecord(val reportId: String, val timezone: String)

/** Passes records to acme.telematics.v1.TelemetryReadingService. */
class PropagateDutyStatusReportIdTelemetryReading(private val stub: TelemetryReadingServiceGrpcKt.TelemetryReadingServiceCoroutineStub) : PropagateDutyStatusReportIdTarget {
    override suspend fun deliver(rec: PropagateDutyStatusReportIdRecord) {
        val request = cloneTelemetryReadingRequest {
            this.telemetryReading = telemetryReading { this.correlationId = rec.timezone }
        }
        RpcBoundary.run("acme.telematics.v1.TelemetryReadingService/CloneTelemetryReading") { stub.cloneTelemetryReading(request) }
    }
}

/** Hands records to acme.devices.v1.ManagedDeviceQueryService. */
class PropagateDutyStatusReportIdManagedDeviceQuery(private val stub: ManagedDeviceQueryServiceGrpcKt.ManagedDeviceQueryServiceCoroutineStub) : PropagateDutyStatusReportIdTarget {
    override suspend fun deliver(rec: PropagateDutyStatusReportIdRecord) {
        val request = suggestManagedDevicesRequest {
            this.page = pageRequest { this.pageToken = rec.reportId }
            this.filter = SuggestManagedDevicesRequestKt.filter { this.ownerId = rec.timezone }
        }
        stub.suggestManagedDevices(request)
    }
}
