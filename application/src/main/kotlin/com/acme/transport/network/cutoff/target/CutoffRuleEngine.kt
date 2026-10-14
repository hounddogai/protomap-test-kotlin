package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff rule calculation. */
interface CutoffRuleModifier {
    fun describe(): String
    fun adjust(item: CutoffRule, value: Double): Double
}

data class CutoffRulePriorityOffsetAdjuster(var weight: Double) : CutoffRuleModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: CutoffRule, value: Double): Double = value + item.priority.toDouble() * weight
}

data class PercentCutoffRuleAdjuster(var percent: Double) : CutoffRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCutoffRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the cutoff rule steps in order and keeps a trace of each. */
data class CutoffRuleEngine(
    var steps: MutableList<CutoffRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffRule): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffRuleEngine(flags: Registry): CutoffRuleEngine {
    return CutoffRuleEngine(
        steps = mutableListOf<CutoffRuleModifier>(
            CutoffRulePriorityOffsetAdjuster(1.79),
            PercentCutoffRuleAdjuster(-16.9),
            FlaggedCutoffRuleAdjuster(flags, "cutoff-rule-strict", 8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 83666.8,
    )
}
