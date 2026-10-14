package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback dispute rollup calculation. */
interface FallbackDisputeRollupFactor {
    fun describe(): String
    fun adjust(item: FallbackDisputeRollup, value: Double): Double
}

data class RoundingFallbackDisputeRollupAdjuster(var step: Double) : FallbackDisputeRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDisputeRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackDisputeRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDisputeRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDisputeRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackDisputeRollupAdjuster(var cap: Double) : FallbackDisputeRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDisputeRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackDisputeRollupAdjuster(var minimum: Double) : FallbackDisputeRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDisputeRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback dispute rollup steps in order and keeps a trace of each. */
data class FallbackDisputeRollupEngine(
    var steps: MutableList<FallbackDisputeRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackDisputeRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDisputeRollupFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDisputeRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackDisputeRollupEngine(flags: Registry): FallbackDisputeRollupEngine {
    return FallbackDisputeRollupEngine(
        steps = mutableListOf<FallbackDisputeRollupFactor>(
            RoundingFallbackDisputeRollupAdjuster(0.5),
            FlaggedFallbackDisputeRollupAdjuster(flags, "fallback-dispute-rollup-rolling", -5.3),
            CapFallbackDisputeRollupAdjuster(33894.0),
            MinimumFallbackDisputeRollupAdjuster(15.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 89807.0,
    )
}
