package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback node variance calculation. */
interface FallbackNodeVarianceModifier {
    fun describe(): String
    fun adjust(item: FallbackNodeVariance, value: Double): Double
}

data class RoundingFallbackNodeVarianceAdjuster(var step: Double) : FallbackNodeVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackNodeVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackNodeVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackNodeVarianceAdjuster(var cap: Double) : FallbackNodeVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackNodeVarianceAdjuster(var minimum: Double) : FallbackNodeVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackNodeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackNodeVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FallbackNodeVarianceStartsAtOffsetAdjuster(var weight: Double) : FallbackNodeVarianceModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FallbackNodeVariance, value: Double): Double = value + item.startsAt.toDouble() * weight
}

/** Runs the fallback node variance steps in order and keeps a trace of each. */
data class FallbackNodeVarianceEngine(
    var steps: MutableList<FallbackNodeVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackNodeVariance): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackNodeVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackNodeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackNodeVarianceEngine(flags: Registry): FallbackNodeVarianceEngine {
    return FallbackNodeVarianceEngine(
        steps = mutableListOf<FallbackNodeVarianceModifier>(
            RoundingFallbackNodeVarianceAdjuster(0.25),
            TieredFallbackNodeVarianceAdjuster(
                mutableListOf<Double>(8016.0, 8642.0),
                mutableListOf<Double>(1.06, 1.2),
            ),
            CapFallbackNodeVarianceAdjuster(32498.0),
            MinimumFallbackNodeVarianceAdjuster(29.8),
            FlaggedFallbackNodeVarianceAdjuster(flags, "fallback-node-variance-tiered", 0.6),
            FallbackNodeVarianceStartsAtOffsetAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 82539.6,
    )
}
