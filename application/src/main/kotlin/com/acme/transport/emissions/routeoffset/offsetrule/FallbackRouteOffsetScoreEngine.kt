package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback route offset score calculation. */
interface FallbackRouteOffsetScoreStep {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetScore, value: Double): Double
}

data class CapFallbackRouteOffsetScoreAdjuster(var cap: Double) : FallbackRouteOffsetScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRouteOffsetScore, value: Double): Double = minOf(value, cap)
}

data class LaneFallbackRouteOffsetScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRouteOffsetScoreStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: FallbackRouteOffsetScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingFallbackRouteOffsetScoreAdjuster(var step: Double) : FallbackRouteOffsetScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRouteOffsetScore, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackRouteOffsetScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRouteOffsetScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRouteOffsetScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback route offset score steps in order and keeps a trace of each. */
data class FallbackRouteOffsetScoreEngine(
    var steps: MutableList<FallbackRouteOffsetScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRouteOffsetScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRouteOffsetScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRouteOffsetScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetScoreEngine(flags: Registry): FallbackRouteOffsetScoreEngine {
    return FallbackRouteOffsetScoreEngine(
        steps = mutableListOf<FallbackRouteOffsetScoreStep>(
            CapFallbackRouteOffsetScoreAdjuster(25997.0),
            LaneFallbackRouteOffsetScoreAdjuster(
                mutableMapOf<String, Double>("online" to 0.76, "central" to 1.23, "east" to 0.83),
            ),
            RoundingFallbackRouteOffsetScoreAdjuster(0.05),
            FlaggedFallbackRouteOffsetScoreAdjuster(flags, "fallback-route-offset-score-legacy", -5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 6794.9,
    )
}
