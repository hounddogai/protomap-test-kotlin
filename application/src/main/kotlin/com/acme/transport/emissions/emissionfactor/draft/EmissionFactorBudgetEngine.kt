package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor budget calculation. */
interface EmissionFactorBudgetStep {
    fun describe(): String
    fun adjust(item: EmissionFactorBudget, value: Double): Double
}

data class RoundingEmissionFactorBudgetAdjuster(var step: Double) : EmissionFactorBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorBudget, value: Double): Double = roundTo(value, step)
}

data class EmissionFactorBudgetPriorityOffsetAdjuster(var weight: Double) : EmissionFactorBudgetStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: EmissionFactorBudget, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedEmissionFactorBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumEmissionFactorBudgetAdjuster(var minimum: Double) : EmissionFactorBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEmissionFactorBudgetAdjuster(var cap: Double) : EmissionFactorBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the emission factor budget steps in order and keeps a trace of each. */
data class EmissionFactorBudgetEngine(
    var steps: MutableList<EmissionFactorBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorBudget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorBudgetEngine(flags: Registry): EmissionFactorBudgetEngine {
    return EmissionFactorBudgetEngine(
        steps = mutableListOf<EmissionFactorBudgetStep>(
            RoundingEmissionFactorBudgetAdjuster(0.25),
            EmissionFactorBudgetPriorityOffsetAdjuster(0.25),
            FlaggedEmissionFactorBudgetAdjuster(flags, "emission-factor-budget-legacy", -3.1),
            MinimumEmissionFactorBudgetAdjuster(39.6),
            CapEmissionFactorBudgetAdjuster(23587.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 66381.5,
    )
}
