package com.acme.transport.ops.crewingreconciler

import com.acme.dispatch.v1.DispatchAssignmentValidationServiceGrpcKt
import com.acme.dispatch.v1.previewDispatchAssignmentRequest
import com.acme.routing.v1.RouteCalculationServiceGrpcKt
import com.acme.routing.v1.explainRouteCalculationRequest
import com.acme.routing.v1.route
import com.acme.sponsorships.v1.SponsorQueryServiceGrpcKt
import com.acme.sponsorships.v1.resolveSponsorsRequest
import com.acme.telematics.v1.TelemetryReadingCalculationServiceGrpcKt
import com.acme.telematics.v1.simulateTelemetryReadingRequest
import com.acme.telematics.v1.telemetryReading
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.sponsorships.v1.SponsorQueryService/ResolveSponsors and hands what it keeps to the sink of its kind. */
suspend fun relaySponsorsMessage(channel: ManagedChannel) {
    val upstream = SponsorQueryServiceGrpcKt.SponsorQueryServiceCoroutineStub(channel)
    val reply = RpcBoundary.read { upstream.resolveSponsors(resolveSponsorsRequest { }) }
    val entry = RelaySponsorsMessageRecord(
        message = reply.result.message,
        correlationId = reply.result.correlationId,
    )
    val registry = mutableMapOf<String, RelaySponsorsMessageWriter>()
    for (registration in listOf(
        RelaySponsorsMessageRegistration("telemetry-reading-calculation", RelaySponsorsMessageTelemetryReadingCalculation(TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub(channel))),
        RelaySponsorsMessageRegistration("route-calculation", RelaySponsorsMessageRouteCalculation(RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub(channel))),
        RelaySponsorsMessageRegistration("dispatch-assignment-validation", RelaySponsorsMessageDispatchAssignmentValidation(DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["route-calculation"] ?: return
    sink.forward(entry)
}

/** Where relaySponsorsMessage hands each record. */
interface RelaySponsorsMessageWriter {
    suspend fun forward(rec: RelaySponsorsMessageRecord)
}

/** What relaySponsorsMessage keeps of each ResolveSponsors response. */
data class RelaySponsorsMessageRecord(val message: String, val correlationId: String)

/** Writes records to acme.telematics.v1.TelemetryReadingCalculationService. */
class RelaySponsorsMessageTelemetryReadingCalculation(private val stub: TelemetryReadingCalculationServiceGrpcKt.TelemetryReadingCalculationServiceCoroutineStub) : RelaySponsorsMessageWriter {
    override suspend fun forward(rec: RelaySponsorsMessageRecord) {
        val request = simulateTelemetryReadingRequest {
            this.telemetryReading = telemetryReading { this.kind = rec.correlationId }
        }
        stub.simulateTelemetryReading(request)
    }
}

/** Writes records to acme.routing.v1.RouteCalculationService. */
class RelaySponsorsMessageRouteCalculation(private val stub: RouteCalculationServiceGrpcKt.RouteCalculationServiceCoroutineStub) : RelaySponsorsMessageWriter {
    override suspend fun forward(rec: RelaySponsorsMessageRecord) {
        stub.explainRouteCalculation(explainRouteCalculationRequest { this.route = route { this.notes = rec.correlationId } })
    }
}

/** Passes records to acme.dispatch.v1.DispatchAssignmentValidationService. */
class RelaySponsorsMessageDispatchAssignmentValidation(private val stub: DispatchAssignmentValidationServiceGrpcKt.DispatchAssignmentValidationServiceCoroutineStub) : RelaySponsorsMessageWriter {
    override suspend fun forward(rec: RelaySponsorsMessageRecord) {
        stub.previewDispatchAssignment(previewDispatchAssignmentRequest { this.reason = rec.message })
    }
}

/** A sink and the kind of record it takes. */
class RelaySponsorsMessageRegistration(val kind: String, val sink: RelaySponsorsMessageWriter)
