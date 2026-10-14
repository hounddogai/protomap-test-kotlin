package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal attempt estimate calculation. */
interface SeasonalAttemptEstimateModifier {
    fun describe(): String
    fun adjust(item: SeasonalAttemptEstimate, value: Double): Double
}

data class MinimumSeasonalAttemptEstimateAdjuster(var minimum: Double) : SeasonalAttemptEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAttemptEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalAttemptEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAttemptEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAttemptEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalAttemptEstimateAdjuster(var percent: Double) : SeasonalAttemptEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAttemptEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SeasonalAttemptEstimateEtaMinutesOffsetAdjuster(
    var weight: Double,
) : SeasonalAttemptEstimateModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: SeasonalAttemptEstimate, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class ChannelSeasonalAttemptEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAttemptEstimateModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: SeasonalAttemptEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal attempt estimate steps in order and keeps a trace of each. */
data class SeasonalAttemptEstimateEngine(
    var steps: MutableList<SeasonalAttemptEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalAttemptEstimate): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAttemptEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAttemptEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalAttemptEstimateEngine(flags: Registry): SeasonalAttemptEstimateEngine {
    return SeasonalAttemptEstimateEngine(
        steps = mutableListOf<SeasonalAttemptEstimateModifier>(
            MinimumSeasonalAttemptEstimateAdjuster(41.7),
            FlaggedSeasonalAttemptEstimateAdjuster(flags, "seasonal-attempt-estimate-rolling", 3.8),
            PercentSeasonalAttemptEstimateAdjuster(-14.2),
            SeasonalAttemptEstimateEtaMinutesOffsetAdjuster(0.2),
            ChannelSeasonalAttemptEstimateAdjuster(
                mutableMapOf<String, Double>("west" to 0.87, "east" to 0.97, "retail" to 0.79),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 48595.3,
    )
}
