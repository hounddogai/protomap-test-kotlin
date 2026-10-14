package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tire budget calculation. */
interface ManualTireBudgetStep {
    fun describe(): String
    fun adjust(item: ManualTireBudget, value: Double): Double
}

data class RoundingManualTireBudgetAdjuster(var step: Double) : ManualTireBudgetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTireBudget, value: Double): Double = roundTo(value, step)
}

data class NameManualTireBudgetAdjuster(var factors: MutableMap<String, Double>) : ManualTireBudgetStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualTireBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualTireBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTireBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTireBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualTireBudgetAdjuster(var cap: Double) : ManualTireBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTireBudget, value: Double): Double = minOf(value, cap)
}

data class ManualTireBudgetDurationMinutesOffsetAdjuster(var weight: Double) : ManualTireBudgetStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ManualTireBudget, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the manual tire budget steps in order and keeps a trace of each. */
data class ManualTireBudgetEngine(
    var steps: MutableList<ManualTireBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTireBudget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTireBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTireBudgetEngine(): ManualTireBudgetEngine {
    return ManualTireBudgetEngine(
        steps = mutableListOf<ManualTireBudgetStep>(
            RoundingManualTireBudgetAdjuster(0.5),
            NameManualTireBudgetAdjuster(mutableMapOf<String, Double>("west" to 0.86)),
            TieredManualTireBudgetAdjuster(
                mutableListOf<Double>(3376.0, 5528.0, 6961.0),
                mutableListOf<Double>(0.95, 1.04, 0.81),
            ),
            CapManualTireBudgetAdjuster(47359.0),
            ManualTireBudgetDurationMinutesOffsetAdjuster(1.78),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 98682.9,
    )
}
