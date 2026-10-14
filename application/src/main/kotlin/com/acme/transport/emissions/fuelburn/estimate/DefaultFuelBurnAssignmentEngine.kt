package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default fuel burn assignment calculation. */
interface DefaultFuelBurnAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double
}

data class PercentDefaultFuelBurnAssignmentAdjuster(var percent: Double) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultFuelBurnAssignmentAdjuster(var minimum: Double) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultFuelBurnAssignmentFuelLitersOffsetAdjuster(
    var weight: Double,
) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedDefaultFuelBurnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneDefaultFuelBurnAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultFuelBurnAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultFuelBurnAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultFuelBurnAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default fuel burn assignment steps in order and keeps a trace of each. */
data class DefaultFuelBurnAssignmentEngine(
    var steps: MutableList<DefaultFuelBurnAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultFuelBurnAssignment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultFuelBurnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultFuelBurnAssignmentEngine(flags: Registry): DefaultFuelBurnAssignmentEngine {
    return DefaultFuelBurnAssignmentEngine(
        steps = mutableListOf<DefaultFuelBurnAssignmentAdjuster>(
            PercentDefaultFuelBurnAssignmentAdjuster(8.9),
            MinimumDefaultFuelBurnAssignmentAdjuster(48.4),
            DefaultFuelBurnAssignmentFuelLitersOffsetAdjuster(0.41),
            FlaggedDefaultFuelBurnAssignmentAdjuster(flags, "default-fuel-burn-assignment-primary", -1.4),
            LaneDefaultFuelBurnAssignmentAdjuster(mutableMapOf<String, Double>("east" to 1.06)),
            TieredDefaultFuelBurnAssignmentAdjuster(
                mutableListOf<Double>(3801.0, 9217.0),
                mutableListOf<Double>(1.12, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 32310.3,
    )
}
