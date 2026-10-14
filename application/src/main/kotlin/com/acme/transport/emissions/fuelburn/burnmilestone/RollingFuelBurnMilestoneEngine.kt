package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling fuel burn milestone calculation. */
interface RollingFuelBurnMilestoneFactor {
    fun describe(): String
    fun adjust(item: RollingFuelBurnMilestone, value: Double): Double
}

data class FlaggedRollingFuelBurnMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingFuelBurnMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingFuelBurnMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingFuelBurnMilestoneAdjuster(var percent: Double) : RollingFuelBurnMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingFuelBurnMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingFuelBurnMilestoneAdjuster(var step: Double) : RollingFuelBurnMilestoneFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelBurnMilestone, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling fuel burn milestone steps in order and keeps a trace of each. */
data class RollingFuelBurnMilestoneEngine(
    var steps: MutableList<RollingFuelBurnMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFuelBurnMilestone): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingFuelBurnMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingFuelBurnMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnMilestoneEngine(flags: Registry): RollingFuelBurnMilestoneEngine {
    return RollingFuelBurnMilestoneEngine(
        steps = mutableListOf<RollingFuelBurnMilestoneFactor>(
            FlaggedRollingFuelBurnMilestoneAdjuster(flags, "rolling-fuel-burn-milestone-regional", -1.9),
            PercentRollingFuelBurnMilestoneAdjuster(0.6),
            RoundingRollingFuelBurnMilestoneAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 21083.2,
    )
}
