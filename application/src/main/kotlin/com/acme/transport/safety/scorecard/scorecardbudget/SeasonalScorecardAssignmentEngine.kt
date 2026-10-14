package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal scorecard assignment calculation. */
interface SeasonalScorecardAssignmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalScorecardAssignment, value: Double): Double
}

data class TieredSeasonalScorecardAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalScorecardAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalScorecardAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalScorecardAssignmentAdjuster(var cap: Double) : SeasonalScorecardAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalScorecardAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalScorecardAssignmentAdjuster(var percent: Double) : SeasonalScorecardAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalScorecardAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalScorecardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalScorecardAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalScorecardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalScorecardAssignmentAdjuster(var step: Double) : SeasonalScorecardAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalScorecardAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal scorecard assignment steps in order and keeps a trace of each. */
data class SeasonalScorecardAssignmentEngine(
    var steps: MutableList<SeasonalScorecardAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalScorecardAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalScorecardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalScorecardAssignmentEngine(flags: Registry): SeasonalScorecardAssignmentEngine {
    return SeasonalScorecardAssignmentEngine(
        steps = mutableListOf<SeasonalScorecardAssignmentFactor>(
            TieredSeasonalScorecardAssignmentAdjuster(
                mutableListOf<Double>(1042.0, 1396.0),
                mutableListOf<Double>(0.91, 1.0),
            ),
            CapSeasonalScorecardAssignmentAdjuster(42308.0),
            PercentSeasonalScorecardAssignmentAdjuster(19.8),
            FlaggedSeasonalScorecardAssignmentAdjuster(flags, "seasonal-scorecard-assignment-seasonal", -7.7),
            RoundingSeasonalScorecardAssignmentAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 1811.4,
    )
}
