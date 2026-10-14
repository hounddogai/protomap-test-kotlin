package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell rule calculation. */
interface DwellRuleModifier {
    fun describe(): String
    fun adjust(item: DwellRule, value: Double): Double
}

data class MinimumDwellRuleAdjuster(var minimum: Double) : DwellRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDwellRuleAdjuster(var step: Double) : DwellRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellRule, value: Double): Double = roundTo(value, step)
}

data class CapDwellRuleAdjuster(var cap: Double) : DwellRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellRule, value: Double): Double = minOf(value, cap)
}

data class TieredDwellRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DwellRuleMarginOffsetAdjuster(var weight: Double) : DwellRuleModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DwellRule, value: Double): Double = value + item.margin * weight
}

/** Runs the dwell rule steps in order and keeps a trace of each. */
data class DwellRuleEngine(
    var steps: MutableList<DwellRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellRule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellRuleEngine(): DwellRuleEngine {
    return DwellRuleEngine(
        steps = mutableListOf<DwellRuleModifier>(
            MinimumDwellRuleAdjuster(45.0),
            RoundingDwellRuleAdjuster(0.1),
            CapDwellRuleAdjuster(18973.0),
            TieredDwellRuleAdjuster(
                mutableListOf<Double>(978.0, 1499.0, 1548.0),
                mutableListOf<Double>(1.1, 0.82, 0.97),
            ),
            DwellRuleMarginOffsetAdjuster(0.04),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 18875.2,
    )
}
