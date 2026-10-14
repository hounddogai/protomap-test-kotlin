package com.acme.transport.network.node.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal node plan calculation. */
interface SeasonalNodePlanAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalNodePlan, value: Double): Double
}

data class PercentSeasonalNodePlanAdjuster(var percent: Double) : SeasonalNodePlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalNodePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalNodePlanAdjuster(var step: Double) : SeasonalNodePlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalNodePlan, value: Double): Double = roundTo(value, step)
}

data class TieredSeasonalNodePlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalNodePlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalNodePlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalNodePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalNodePlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalNodePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal node plan steps in order and keeps a trace of each. */
data class SeasonalNodePlanEngine(
    var steps: MutableList<SeasonalNodePlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalNodePlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalNodePlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalNodePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalNodePlanEngine(flags: Registry): SeasonalNodePlanEngine {
    return SeasonalNodePlanEngine(
        steps = mutableListOf<SeasonalNodePlanAdjuster>(
            PercentSeasonalNodePlanAdjuster(-12.8),
            RoundingSeasonalNodePlanAdjuster(0.1),
            TieredSeasonalNodePlanAdjuster(
                mutableListOf<Double>(1082.0, 1328.0),
                mutableListOf<Double>(1.05, 1.14),
            ),
            FlaggedSeasonalNodePlanAdjuster(flags, "seasonal-node-plan-global", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 84548.2,
    )
}
