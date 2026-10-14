package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal near miss assignment calculation. */
interface SeasonalNearMissAssignmentFactor {
    fun describe(): String
    fun adjust(item: SeasonalNearMissAssignment, value: Double): Double
}

data class MinimumSeasonalNearMissAssignmentAdjuster(var minimum: Double) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalNearMissAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSeasonalNearMissAssignmentAdjuster(var percent: Double) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameSeasonalNearMissAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalNearMissAssignmentAdjuster(var step: Double) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalNearMissAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalNearMissAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalNearMissAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal near miss assignment steps in order and keeps a trace of each. */
data class SeasonalNearMissAssignmentEngine(
    var steps: MutableList<SeasonalNearMissAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalNearMissAssignment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalNearMissAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalNearMissAssignmentEngine(flags: Registry): SeasonalNearMissAssignmentEngine {
    return SeasonalNearMissAssignmentEngine(
        steps = mutableListOf<SeasonalNearMissAssignmentFactor>(
            MinimumSeasonalNearMissAssignmentAdjuster(18.9),
            FlaggedSeasonalNearMissAssignmentAdjuster(flags, "seasonal-near-miss-assignment-seasonal", -3.0),
            PercentSeasonalNearMissAssignmentAdjuster(23.8),
            NameSeasonalNearMissAssignmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.31, "east" to 1.05, "south" to 1.24),
            ),
            RoundingSeasonalNearMissAssignmentAdjuster(1.0),
            TieredSeasonalNearMissAssignmentAdjuster(
                mutableListOf<Double>(2399.0, 4983.0, 6286.0, 6364.0),
                mutableListOf<Double>(0.91, 1.04, 1.08, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 27698.4,
    )
}
