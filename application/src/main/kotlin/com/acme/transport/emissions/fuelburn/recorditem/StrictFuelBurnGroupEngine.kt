package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict fuel burn group calculation. */
interface StrictFuelBurnGroupAdjuster {
    fun describe(): String
    fun adjust(item: StrictFuelBurnGroup, value: Double): Double
}

data class MinimumStrictFuelBurnGroupAdjuster(var minimum: Double) : StrictFuelBurnGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictFuelBurnGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentStrictFuelBurnGroupAdjuster(var percent: Double) : StrictFuelBurnGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictFuelBurnGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictFuelBurnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictFuelBurnGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictFuelBurnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitStrictFuelBurnGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictFuelBurnGroupAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictFuelBurnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictFuelBurnGroupAdjuster(var step: Double) : StrictFuelBurnGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictFuelBurnGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the strict fuel burn group steps in order and keeps a trace of each. */
data class StrictFuelBurnGroupEngine(
    var steps: MutableList<StrictFuelBurnGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictFuelBurnGroup): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictFuelBurnGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictFuelBurnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictFuelBurnGroupEngine(flags: Registry): StrictFuelBurnGroupEngine {
    return StrictFuelBurnGroupEngine(
        steps = mutableListOf<StrictFuelBurnGroupAdjuster>(
            MinimumStrictFuelBurnGroupAdjuster(43.8),
            PercentStrictFuelBurnGroupAdjuster(9.9),
            FlaggedStrictFuelBurnGroupAdjuster(flags, "strict-fuel-burn-group-rolling", 9.5),
            UnitStrictFuelBurnGroupAdjuster(mutableMapOf<String, Double>("online" to 0.96)),
            RoundingStrictFuelBurnGroupAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 59823.6,
    )
}
