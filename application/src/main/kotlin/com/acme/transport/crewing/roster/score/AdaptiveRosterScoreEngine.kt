package com.acme.transport.crewing.roster.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive roster score calculation. */
interface AdaptiveRosterScoreStep {
    fun describe(): String
    fun adjust(item: AdaptiveRosterScore, value: Double): Double
}

data class MinimumAdaptiveRosterScoreAdjuster(var minimum: Double) : AdaptiveRosterScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveRosterScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveRosterScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveRosterScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveRosterScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveRosterScoreAdjuster(var step: Double) : AdaptiveRosterScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRosterScore, value: Double): Double = roundTo(value, step)
}

data class SourceAdaptiveRosterScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveRosterScoreStep {
    override fun describe(): String = "by source"

    override fun adjust(item: AdaptiveRosterScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveRosterScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRosterScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRosterScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive roster score steps in order and keeps a trace of each. */
data class AdaptiveRosterScoreEngine(
    var steps: MutableList<AdaptiveRosterScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveRosterScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveRosterScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveRosterScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveRosterScoreEngine(flags: Registry): AdaptiveRosterScoreEngine {
    return AdaptiveRosterScoreEngine(
        steps = mutableListOf<AdaptiveRosterScoreStep>(
            MinimumAdaptiveRosterScoreAdjuster(13.0),
            FlaggedAdaptiveRosterScoreAdjuster(flags, "adaptive-roster-score-lenient", 6.7),
            RoundingAdaptiveRosterScoreAdjuster(0.5),
            SourceAdaptiveRosterScoreAdjuster(mutableMapOf<String, Double>("east" to 0.99)),
            TieredAdaptiveRosterScoreAdjuster(
                mutableListOf<Double>(3266.0, 7533.0),
                mutableListOf<Double>(1.04, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 98837.0,
    )
}
