package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual clearance rule calculation. */
interface ManualClearanceRuleModifier {
    fun describe(): String
    fun adjust(item: ManualClearanceRule, value: Double): Double
}

data class LabelManualClearanceRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualClearanceRuleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualClearanceRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualClearanceRuleAdjuster(var cap: Double) : ManualClearanceRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualClearanceRule, value: Double): Double = minOf(value, cap)
}

data class MinimumManualClearanceRuleAdjuster(var minimum: Double) : ManualClearanceRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualClearanceRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual clearance rule steps in order and keeps a trace of each. */
data class ManualClearanceRuleEngine(
    var steps: MutableList<ManualClearanceRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualClearanceRule): Double {
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

    fun evaluateAll(items: MutableList<ManualClearanceRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualClearanceRuleEngine(): ManualClearanceRuleEngine {
    return ManualClearanceRuleEngine(
        steps = mutableListOf<ManualClearanceRuleModifier>(
            LabelManualClearanceRuleAdjuster(mutableMapOf<String, Double>("central" to 1.32)),
            CapManualClearanceRuleAdjuster(5481.0),
            MinimumManualClearanceRuleAdjuster(25.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 59540.2,
    )
}
