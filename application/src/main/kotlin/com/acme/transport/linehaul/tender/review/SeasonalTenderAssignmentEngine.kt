package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal tender assignment calculation. */
interface SeasonalTenderAssignmentStep {
    fun describe(): String
    fun adjust(item: SeasonalTenderAssignment, value: Double): Double
}

data class FlaggedSeasonalTenderAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalTenderAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelSeasonalTenderAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalTenderAssignmentAdjuster(var percent: Double) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalTenderAssignmentAdjuster(var step: Double) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalTenderAssignmentAdjuster(var minimum: Double) : SeasonalTenderAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalTenderAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal tender assignment steps in order and keeps a trace of each. */
data class SeasonalTenderAssignmentEngine(
    var steps: MutableList<SeasonalTenderAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalTenderAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTenderAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTenderAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalTenderAssignmentEngine(flags: Registry): SeasonalTenderAssignmentEngine {
    return SeasonalTenderAssignmentEngine(
        steps = mutableListOf<SeasonalTenderAssignmentStep>(
            FlaggedSeasonalTenderAssignmentAdjuster(flags, "seasonal-tender-assignment-default", 6.5),
            TieredSeasonalTenderAssignmentAdjuster(
                mutableListOf<Double>(3157.0, 4734.0),
                mutableListOf<Double>(1.02, 0.94),
            ),
            LabelSeasonalTenderAssignmentAdjuster(mutableMapOf<String, Double>("south" to 1.2, "east" to 1.25)),
            PercentSeasonalTenderAssignmentAdjuster(-13.9),
            RoundingSeasonalTenderAssignmentAdjuster(5.0),
            MinimumSeasonalTenderAssignmentAdjuster(36.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 28393.1,
    )
}
