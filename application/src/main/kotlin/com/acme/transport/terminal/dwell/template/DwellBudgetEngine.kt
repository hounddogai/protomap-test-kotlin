package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell budget calculation. */
interface DwellBudgetAdjuster {
    fun describe(): String
    fun adjust(item: DwellBudget, value: Double): Double
}

data class CategoryDwellBudgetAdjuster(var factors: MutableMap<String, Double>) : DwellBudgetAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: DwellBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDwellBudgetAdjuster(var step: Double) : DwellBudgetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellBudget, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellBudgetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDwellBudgetAdjuster(var percent: Double) : DwellBudgetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDwellBudgetAdjuster(var cap: Double) : DwellBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the dwell budget steps in order and keeps a trace of each. */
data class DwellBudgetEngine(
    var steps: MutableList<DwellBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellBudgetEngine(flags: Registry): DwellBudgetEngine {
    return DwellBudgetEngine(
        steps = mutableListOf<DwellBudgetAdjuster>(
            CategoryDwellBudgetAdjuster(mutableMapOf<String, Double>("online" to 0.84)),
            RoundingDwellBudgetAdjuster(0.05),
            FlaggedDwellBudgetAdjuster(flags, "dwell-budget-legacy", -0.4),
            PercentDwellBudgetAdjuster(20.7),
            CapDwellBudgetAdjuster(1584.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 16297.4,
    )
}
