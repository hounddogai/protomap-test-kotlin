package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dock door rule calculation. */
interface DockDoorRuleStep {
    fun describe(): String
    fun adjust(item: DockDoorRule, value: Double): Double
}

data class PercentDockDoorRuleAdjuster(var percent: Double) : DockDoorRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDockDoorRuleAdjuster(var minimum: Double) : DockDoorRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDockDoorRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDockDoorRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDockDoorRuleAdjuster(var step: Double) : DockDoorRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorRule, value: Double): Double = roundTo(value, step)
}

/** Runs the dock door rule steps in order and keeps a trace of each. */
data class DockDoorRuleEngine(
    var steps: MutableList<DockDoorRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorRule): Double {
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

    fun evaluateAll(items: MutableList<DockDoorRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorRuleEngine(flags: Registry): DockDoorRuleEngine {
    return DockDoorRuleEngine(
        steps = mutableListOf<DockDoorRuleStep>(
            PercentDockDoorRuleAdjuster(4.9),
            MinimumDockDoorRuleAdjuster(27.8),
            TieredDockDoorRuleAdjuster(
                mutableListOf<Double>(1009.0, 3695.0, 4906.0, 7712.0),
                mutableListOf<Double>(0.88, 0.98, 0.91, 1.04),
            ),
            FlaggedDockDoorRuleAdjuster(flags, "dock-door-rule-lenient", 7.8),
            RoundingDockDoorRuleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 25468.7,
    )
}
