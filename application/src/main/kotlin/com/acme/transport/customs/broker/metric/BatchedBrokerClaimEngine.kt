package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched broker claim calculation. */
interface BatchedBrokerClaimAdjuster {
    fun describe(): String
    fun adjust(item: BatchedBrokerClaim, value: Double): Double
}

data class FlaggedBatchedBrokerClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentBatchedBrokerClaimAdjuster(var percent: Double) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBatchedBrokerClaimAdjuster(var cap: Double) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumBatchedBrokerClaimAdjuster(var minimum: Double) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BatchedBrokerClaimFloorValueOffsetAdjuster(var weight: Double) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double = value + item.floorValue * weight
}

data class RoundingBatchedBrokerClaimAdjuster(var step: Double) : BatchedBrokerClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedBrokerClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the batched broker claim steps in order and keeps a trace of each. */
data class BatchedBrokerClaimEngine(
    var steps: MutableList<BatchedBrokerClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedBrokerClaim): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedBrokerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedBrokerClaimEngine(flags: Registry): BatchedBrokerClaimEngine {
    return BatchedBrokerClaimEngine(
        steps = mutableListOf<BatchedBrokerClaimAdjuster>(
            FlaggedBatchedBrokerClaimAdjuster(flags, "batched-broker-claim-rolling", 1.4),
            PercentBatchedBrokerClaimAdjuster(-21.8),
            CapBatchedBrokerClaimAdjuster(13627.0),
            MinimumBatchedBrokerClaimAdjuster(29.6),
            BatchedBrokerClaimFloorValueOffsetAdjuster(1.98),
            RoundingBatchedBrokerClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 67957.7,
    )
}
