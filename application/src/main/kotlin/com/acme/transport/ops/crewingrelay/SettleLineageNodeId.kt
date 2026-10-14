package com.acme.transport.ops.crewingrelay

import com.acme.featureflags.v1.FeatureFlagServiceGrpcKt
import com.acme.featureflags.v1.ListFeatureFlagsRequestKt
import com.acme.featureflags.v1.listFeatureFlagsRequest
import com.acme.fuel.v1.FuelPolicyValidationServiceGrpcKt
import com.acme.fuel.v1.fuelPolicy
import com.acme.fuel.v1.validateFuelPolicyRequest
import com.acme.lineage.v1.LineageNodeCalculationServiceGrpcKt
import com.acme.lineage.v1.calculateLineageNodeRequest
import com.acme.metrics.v1.ListMetricSeriesesRequestKt
import com.acme.metrics.v1.MetricSeriesServiceGrpcKt
import com.acme.metrics.v1.listMetricSeriesesRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.lineage.v1.LineageNodeCalculationService/CalculateLineageNode and hands what it keeps to the sink of its kind. */
suspend fun settleLineageNodeId(channel: ManagedChannel) {
    val source = LineageNodeCalculationServiceGrpcKt.LineageNodeCalculationServiceCoroutineStub(channel)
    val reply = source.calculateLineageNode(calculateLineageNodeRequest { this.lineageNodeId = "nightly-settle-lineage-node-id" })
    val entry = SettleLineageNodeIdRecord(
        correlationId = reply.result.correlationId,
        currencyCode = reply.breakdown.currencyCode,
        lineageNodeId = normalize(reply.lineageNode.lineageNodeId),
    )
    val sinks = listOf<SettleLineageNodeIdWriter>(
        SettleLineageNodeIdFuelPolicyValidation(FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub(channel)),
        SettleLineageNodeIdFeatureFlag(FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub(channel)),
        SettleLineageNodeIdMetricSeries(MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub(channel)),
    )
    sinks[0].write(entry)
}

/** Where settleLineageNodeId hands each record. */
interface SettleLineageNodeIdWriter {
    suspend fun write(rec: SettleLineageNodeIdRecord)
}

/** What settleLineageNodeId keeps of each CalculateLineageNode response. */
data class SettleLineageNodeIdRecord(val correlationId: String, val currencyCode: String, val lineageNodeId: String)

/** Passes records to acme.fuel.v1.FuelPolicyValidationService. */
class SettleLineageNodeIdFuelPolicyValidation(private val stub: FuelPolicyValidationServiceGrpcKt.FuelPolicyValidationServiceCoroutineStub) : SettleLineageNodeIdWriter {
    override suspend fun write(rec: SettleLineageNodeIdRecord) {
        val request = validateFuelPolicyRequest {
            this.fuelPolicy = fuelPolicy { this.description = rec.currencyCode }
        }
        stub.validateFuelPolicy(request)
    }
}

/** Hands records to acme.featureflags.v1.FeatureFlagService. */
class SettleLineageNodeIdFeatureFlag(private val stub: FeatureFlagServiceGrpcKt.FeatureFlagServiceCoroutineStub) : SettleLineageNodeIdWriter {
    override suspend fun write(rec: SettleLineageNodeIdRecord) {
        val request = listFeatureFlagsRequest {
            this.filter = ListFeatureFlagsRequestKt.filter { this.region = rec.lineageNodeId }
        }
        RpcBoundary.run("acme.featureflags.v1.FeatureFlagService/ListFeatureFlags") { stub.listFeatureFlags(request) }
    }
}

/** Hands records to acme.metrics.v1.MetricSeriesService. */
class SettleLineageNodeIdMetricSeries(private val stub: MetricSeriesServiceGrpcKt.MetricSeriesServiceCoroutineStub) : SettleLineageNodeIdWriter {
    override suspend fun write(rec: SettleLineageNodeIdRecord) {
        val request = listMetricSeriesesRequest {
            this.orderBy = rec.currencyCode
            this.filter = ListMetricSeriesesRequestKt.filter { this.region = rec.lineageNodeId }
        }
        stub.listMetricSerieses(request)
    }
}
