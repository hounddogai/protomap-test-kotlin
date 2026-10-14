package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default dwell budget calculation. */
interface DefaultDwellBudgetStep {
    fun describe(): String
    fun adjust(item: DefaultDwellBudget, value: Double): Double
}

data class CapDefaultDwellBudgetAdjuster(var cap: Double) : DefaultDwellBudgetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultDwellBudget, value: Double): Double = minOf(value, cap)
}

data class FlaggedDefaultDwellBudgetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultDwellBudgetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultDwellBudget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultDwellBudgetAdjuster(var percent: Double) : DefaultDwellBudgetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDwellBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default dwell budget steps in order and keeps a trace of each. */
data class DefaultDwellBudgetEngine(
    var steps: MutableList<DefaultDwellBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultDwellBudget): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultDwellBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultDwellBudgetEngine(flags: Registry): DefaultDwellBudgetEngine {
    return DefaultDwellBudgetEngine(
        steps = mutableListOf<DefaultDwellBudgetStep>(
            CapDefaultDwellBudgetAdjuster(12172.0),
            FlaggedDefaultDwellBudgetAdjuster(flags, "default-dwell-budget-pending", -1.7),
            PercentDefaultDwellBudgetAdjuster(-4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 4457.6,
    )
}
