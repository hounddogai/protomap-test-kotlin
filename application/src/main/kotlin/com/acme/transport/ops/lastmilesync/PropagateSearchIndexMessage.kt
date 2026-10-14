package com.acme.transport.ops.lastmilesync

import com.acme.fuel.v1.FuelCardCalculationServiceGrpcKt
import com.acme.fuel.v1.explainFuelCardCalculationRequest
import com.acme.fuel.v1.fuelCard
import com.acme.pipelines.v1.PipelineCalculationServiceGrpcKt
import com.acme.pipelines.v1.pipeline
import com.acme.pipelines.v1.simulatePipelineRequest
import com.acme.search.v1.SearchIndexValidationServiceGrpcKt
import com.acme.search.v1.validateSearchIndexRequest
import com.acme.telematics.v1.TripLogReportingServiceGrpcKt
import com.acme.telematics.v1.fetchTripLogReportRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.search.v1.SearchIndexValidationService/ValidateSearchIndex and hands what it keeps to the sink of its kind. */
suspend fun propagateSearchIndexMessage(channel: ManagedChannel) {
    val origin = SearchIndexValidationServiceGrpcKt.SearchIndexValidationServiceCoroutineStub(channel)
    val got = RpcBoundary.read { origin.validateSearchIndex(validateSearchIndexRequest { this.reason = "live-propagate-search-index-message" }) }
    val record = PropagateSearchIndexMessageRecord(
        message = got.outcome.message,
        name = got.searchIndex.name,
        correlationId = got.result.correlationId,
    )
    val registry = mutableMapOf<String, PropagateSearchIndexMessageTarget>()
    for (registration in listOf(
        PropagateSearchIndexMessageRegistration("trip-log-reporting", PropagateSearchIndexMessageTripLogReporting(TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub(channel))),
        PropagateSearchIndexMessageRegistration("pipeline-calculation", PropagateSearchIndexMessagePipelineCalculation(PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub(channel))),
        PropagateSearchIndexMessageRegistration("fuel-card-calculation", PropagateSearchIndexMessageFuelCardCalculation(FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["trip-log-reporting"] ?: return
    sink.relay(record)
}

/** Where propagateSearchIndexMessage hands each record. */
interface PropagateSearchIndexMessageTarget {
    suspend fun relay(rec: PropagateSearchIndexMessageRecord)
}

/** What propagateSearchIndexMessage keeps of each ValidateSearchIndex response. */
data class PropagateSearchIndexMessageRecord(val message: String, val name: String, val correlationId: String)

/** Sends records to acme.telematics.v1.TripLogReportingService. */
class PropagateSearchIndexMessageTripLogReporting(private val stub: TripLogReportingServiceGrpcKt.TripLogReportingServiceCoroutineStub) : PropagateSearchIndexMessageTarget {
    override suspend fun relay(rec: PropagateSearchIndexMessageRecord) {
        stub.fetchTripLogReport(fetchTripLogReportRequest { this.tripLogId = rec.message })
    }
}

/** Writes records to acme.pipelines.v1.PipelineCalculationService. */
class PropagateSearchIndexMessagePipelineCalculation(private val stub: PipelineCalculationServiceGrpcKt.PipelineCalculationServiceCoroutineStub) : PropagateSearchIndexMessageTarget {
    override suspend fun relay(rec: PropagateSearchIndexMessageRecord) {
        val request = simulatePipelineRequest {
            this.pipeline = pipeline { this.pipelineId = rec.message }
            this.pipelineId = rec.correlationId
        }
        RpcBoundary.run("acme.pipelines.v1.PipelineCalculationService/SimulatePipeline") { stub.simulatePipeline(request) }
    }
}

/** Hands records to acme.fuel.v1.FuelCardCalculationService. */
class PropagateSearchIndexMessageFuelCardCalculation(private val stub: FuelCardCalculationServiceGrpcKt.FuelCardCalculationServiceCoroutineStub) : PropagateSearchIndexMessageTarget {
    override suspend fun relay(rec: PropagateSearchIndexMessageRecord) {
        val request = explainFuelCardCalculationRequest {
            this.fuelCard = fuelCard { this.fuelCardId = rec.name }
            this.fuelCardId = rec.message
            this.reason = rec.correlationId
        }
        RpcBoundary.run("acme.fuel.v1.FuelCardCalculationService/ExplainFuelCardCalculation") { stub.explainFuelCardCalculation(request) }
    }
}

/** A sink and the kind of record it takes. */
class PropagateSearchIndexMessageRegistration(val kind: String, val sink: PropagateSearchIndexMessageTarget)
