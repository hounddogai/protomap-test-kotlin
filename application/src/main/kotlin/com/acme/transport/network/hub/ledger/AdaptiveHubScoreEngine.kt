package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive hub score calculation. */
interface AdaptiveHubScoreFactor {
    fun describe(): String
    fun adjust(item: AdaptiveHubScore, value: Double): Double
}

data class FlaggedAdaptiveHubScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveHubScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveHubScoreEndsAtOffsetAdjuster(var weight: Double) : AdaptiveHubScoreFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentAdaptiveHubScoreAdjuster(var percent: Double) : AdaptiveHubScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveHubScoreAdjuster(var cap: Double) : AdaptiveHubScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveHubScoreAdjuster(var minimum: Double) : AdaptiveHubScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveHubScoreAdjuster(var step: Double) : AdaptiveHubScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveHubScore, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive hub score steps in order and keeps a trace of each. */
data class AdaptiveHubScoreEngine(
    var steps: MutableList<AdaptiveHubScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveHubScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveHubScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveHubScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveHubScoreEngine(flags: Registry): AdaptiveHubScoreEngine {
    return AdaptiveHubScoreEngine(
        steps = mutableListOf<AdaptiveHubScoreFactor>(
            FlaggedAdaptiveHubScoreAdjuster(flags, "adaptive-hub-score-manual", 2.1),
            AdaptiveHubScoreEndsAtOffsetAdjuster(0.38),
            PercentAdaptiveHubScoreAdjuster(16.7),
            CapAdaptiveHubScoreAdjuster(29623.0),
            MinimumAdaptiveHubScoreAdjuster(31.7),
            RoundingAdaptiveHubScoreAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 88180.7,
    )
}
