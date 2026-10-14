package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal qualification milestone calculation. */
interface SeasonalQualificationMilestoneFactor {
    fun describe(): String
    fun adjust(item: SeasonalQualificationMilestone, value: Double): Double
}

data class LocaleSeasonalQualificationMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalQualificationMilestoneAdjuster(
    var percent: Double,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalQualificationMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalQualificationMilestoneAmountOffsetAdjuster(
    var weight: Double,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double = value + item.amount * weight
}

data class MinimumSeasonalQualificationMilestoneAdjuster(
    var minimum: Double,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalQualificationMilestoneAdjuster(
    var step: Double,
) : SeasonalQualificationMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalQualificationMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal qualification milestone steps in order and keeps a trace of each. */
data class SeasonalQualificationMilestoneEngine(
    var steps: MutableList<SeasonalQualificationMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalQualificationMilestone): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalQualificationMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalQualificationMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalQualificationMilestoneEngine(flags: Registry): SeasonalQualificationMilestoneEngine {
    return SeasonalQualificationMilestoneEngine(
        steps = mutableListOf<SeasonalQualificationMilestoneFactor>(
            LocaleSeasonalQualificationMilestoneAdjuster(
                mutableMapOf<String, Double>("online" to 1.25, "south" to 1.38, "north" to 1.38),
            ),
            PercentSeasonalQualificationMilestoneAdjuster(6.5),
            FlaggedSeasonalQualificationMilestoneAdjuster(
                flags,
                "seasonal-qualification-milestone-lenient",
                -8.6,
            ),
            SeasonalQualificationMilestoneAmountOffsetAdjuster(1.61),
            MinimumSeasonalQualificationMilestoneAdjuster(6.1),
            RoundingSeasonalQualificationMilestoneAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 28507.0,
    )
}
