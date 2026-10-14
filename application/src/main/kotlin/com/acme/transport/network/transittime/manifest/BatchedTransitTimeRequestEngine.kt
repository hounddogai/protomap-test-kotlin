package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched transit time request calculation. */
interface BatchedTransitTimeRequestModifier {
    fun describe(): String
    fun adjust(item: BatchedTransitTimeRequest, value: Double): Double
}

data class RoundingBatchedTransitTimeRequestAdjuster(var step: Double) : BatchedTransitTimeRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedTransitTimeRequest, value: Double): Double = roundTo(value, step)
}

data class CapBatchedTransitTimeRequestAdjuster(var cap: Double) : BatchedTransitTimeRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedTransitTimeRequest, value: Double): Double = minOf(value, cap)
}

data class PercentBatchedTransitTimeRequestAdjuster(var percent: Double) : BatchedTransitTimeRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedTransitTimeRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedTransitTimeRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedTransitTimeRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedTransitTimeRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumBatchedTransitTimeRequestAdjuster(var minimum: Double) : BatchedTransitTimeRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedTransitTimeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched transit time request steps in order and keeps a trace of each. */
data class BatchedTransitTimeRequestEngine(
    var steps: MutableList<BatchedTransitTimeRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedTransitTimeRequest): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedTransitTimeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedTransitTimeRequestEngine(flags: Registry): BatchedTransitTimeRequestEngine {
    return BatchedTransitTimeRequestEngine(
        steps = mutableListOf<BatchedTransitTimeRequestModifier>(
            RoundingBatchedTransitTimeRequestAdjuster(5.0),
            CapBatchedTransitTimeRequestAdjuster(20629.0),
            PercentBatchedTransitTimeRequestAdjuster(-14.5),
            FlaggedBatchedTransitTimeRequestAdjuster(flags, "batched-transit-time-request-batched", 0.2),
            MinimumBatchedTransitTimeRequestAdjuster(4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 55602.8,
    )
}
