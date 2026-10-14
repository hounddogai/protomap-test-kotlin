package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel burn assignment calculation. */
interface FuelBurnAssignmentModifier {
    fun describe(): String
    fun adjust(item: FuelBurnAssignment, value: Double): Double
}

data class MinimumFuelBurnAssignmentAdjuster(var minimum: Double) : FuelBurnAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelBurnAssignmentAdjuster(var percent: Double) : FuelBurnAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelBurnAssignmentAdjuster(var step: Double) : FuelBurnAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnAssignment, value: Double): Double = roundTo(value, step)
}

data class FlaggedFuelBurnAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFuelBurnAssignmentAdjuster(var cap: Double) : FuelBurnAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn assignment steps in order and keeps a trace of each. */
data class FuelBurnAssignmentEngine(
    var steps: MutableList<FuelBurnAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FuelBurnAssignment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelBurnAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelBurnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFuelBurnAssignmentEngine(flags: Registry): FuelBurnAssignmentEngine {
    return FuelBurnAssignmentEngine(
        steps = mutableListOf<FuelBurnAssignmentModifier>(
            MinimumFuelBurnAssignmentAdjuster(17.6),
            PercentFuelBurnAssignmentAdjuster(0.6),
            RoundingFuelBurnAssignmentAdjuster(1.0),
            FlaggedFuelBurnAssignmentAdjuster(flags, "fuel-burn-assignment-global", 1.4),
            CapFuelBurnAssignmentAdjuster(48013.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 13940.3,
    )
}
