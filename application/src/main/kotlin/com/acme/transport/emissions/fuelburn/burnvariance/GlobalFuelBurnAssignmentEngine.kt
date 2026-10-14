package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global fuel burn assignment calculation. */
interface GlobalFuelBurnAssignmentModifier {
    fun describe(): String
    fun adjust(item: GlobalFuelBurnAssignment, value: Double): Double
}

data class PercentGlobalFuelBurnAssignmentAdjuster(var percent: Double) : GlobalFuelBurnAssignmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalFuelBurnAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalFuelBurnAssignmentAdjuster(var minimum: Double) : GlobalFuelBurnAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelBurnAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameGlobalFuelBurnAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalFuelBurnAssignmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalFuelBurnAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global fuel burn assignment steps in order and keeps a trace of each. */
data class GlobalFuelBurnAssignmentEngine(
    var steps: MutableList<GlobalFuelBurnAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalFuelBurnAssignment): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalFuelBurnAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalFuelBurnAssignmentEngine(): GlobalFuelBurnAssignmentEngine {
    return GlobalFuelBurnAssignmentEngine(
        steps = mutableListOf<GlobalFuelBurnAssignmentModifier>(
            PercentGlobalFuelBurnAssignmentAdjuster(-15.6),
            MinimumGlobalFuelBurnAssignmentAdjuster(28.8),
            NameGlobalFuelBurnAssignmentAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 93396.2,
    )
}
