package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling scorecard group calculation. */
interface RollingScorecardGroupFactor {
    fun describe(): String
    fun adjust(item: RollingScorecardGroup, value: Double): Double
}

data class LocaleRollingScorecardGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingScorecardGroupFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingScorecardGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingScorecardGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingScorecardGroupCostOffsetAdjuster(var weight: Double) : RollingScorecardGroupFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double = value + item.cost * weight
}

data class RoundingRollingScorecardGroupAdjuster(var step: Double) : RollingScorecardGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double = roundTo(value, step)
}

data class FlaggedRollingScorecardGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingScorecardGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingScorecardGroupAdjuster(var percent: Double) : RollingScorecardGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingScorecardGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling scorecard group steps in order and keeps a trace of each. */
data class RollingScorecardGroupEngine(
    var steps: MutableList<RollingScorecardGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingScorecardGroup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingScorecardGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingScorecardGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingScorecardGroupEngine(flags: Registry): RollingScorecardGroupEngine {
    return RollingScorecardGroupEngine(
        steps = mutableListOf<RollingScorecardGroupFactor>(
            LocaleRollingScorecardGroupAdjuster(
                mutableMapOf<String, Double>("central" to 1.39, "south" to 1.19, "north" to 1.01),
            ),
            TieredRollingScorecardGroupAdjuster(
                mutableListOf<Double>(3018.0, 8230.0),
                mutableListOf<Double>(1.01, 0.93),
            ),
            RollingScorecardGroupCostOffsetAdjuster(0.1),
            RoundingRollingScorecardGroupAdjuster(1.0),
            FlaggedRollingScorecardGroupAdjuster(flags, "rolling-scorecard-group-lenient", 6.1),
            PercentRollingScorecardGroupAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 41523.0,
    )
}
