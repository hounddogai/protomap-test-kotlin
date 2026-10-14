package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled fuel burn cycle calculation. */
interface ScheduledFuelBurnCycleAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledFuelBurnCycle, value: Double): Double
}

data class MinimumScheduledFuelBurnCycleAdjuster(var minimum: Double) : ScheduledFuelBurnCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledFuelBurnCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledFuelBurnCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledFuelBurnCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledFuelBurnCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryScheduledFuelBurnCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledFuelBurnCycleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledFuelBurnCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled fuel burn cycle steps in order and keeps a trace of each. */
data class ScheduledFuelBurnCycleEngine(
    var steps: MutableList<ScheduledFuelBurnCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledFuelBurnCycle): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledFuelBurnCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledFuelBurnCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledFuelBurnCycleEngine(flags: Registry): ScheduledFuelBurnCycleEngine {
    return ScheduledFuelBurnCycleEngine(
        steps = mutableListOf<ScheduledFuelBurnCycleAdjuster>(
            MinimumScheduledFuelBurnCycleAdjuster(7.5),
            FlaggedScheduledFuelBurnCycleAdjuster(flags, "scheduled-fuel-burn-cycle-regional", -1.5),
            CategoryScheduledFuelBurnCycleAdjuster(mutableMapOf<String, Double>("east" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 5109.5,
    )
}
