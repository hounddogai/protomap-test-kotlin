package com.acme.transport.network.node.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback node estimate calculation. */
interface FallbackNodeEstimateStep {
    fun describe(): String
    fun adjust(item: FallbackNodeEstimate, value: Double): Double
}

data class FallbackNodeEstimateDurationMinutesOffsetAdjuster(var weight: Double) : FallbackNodeEstimateStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: FallbackNodeEstimate, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumFallbackNodeEstimateAdjuster(var minimum: Double) : FallbackNodeEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackNodeEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackNodeEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackNodeEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackNodeEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredFallbackNodeEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackNodeEstimateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackNodeEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackNodeEstimateAdjuster(var cap: Double) : FallbackNodeEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackNodeEstimate, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback node estimate steps in order and keeps a trace of each. */
data class FallbackNodeEstimateEngine(
    var steps: MutableList<FallbackNodeEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackNodeEstimate): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackNodeEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackNodeEstimateEngine(flags: Registry): FallbackNodeEstimateEngine {
    return FallbackNodeEstimateEngine(
        steps = mutableListOf<FallbackNodeEstimateStep>(
            FallbackNodeEstimateDurationMinutesOffsetAdjuster(1.25),
            MinimumFallbackNodeEstimateAdjuster(20.9),
            FlaggedFallbackNodeEstimateAdjuster(flags, "fallback-node-estimate-regional", -8.6),
            TieredFallbackNodeEstimateAdjuster(
                mutableListOf<Double>(6443.0, 9540.0),
                mutableListOf<Double>(1.16, 1.09),
            ),
            CapFallbackNodeEstimateAdjuster(22264.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 3691.0,
    )
}
