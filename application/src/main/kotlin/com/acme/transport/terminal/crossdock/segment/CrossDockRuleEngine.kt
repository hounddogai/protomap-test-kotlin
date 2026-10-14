package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the cross dock rule calculation. */
interface CrossDockRuleModifier {
    fun describe(): String
    fun adjust(item: CrossDockRule, value: Double): Double
}

data class PercentCrossDockRuleAdjuster(var percent: Double) : CrossDockRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameCrossDockRuleAdjuster(var factors: MutableMap<String, Double>) : CrossDockRuleModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CrossDockRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCrossDockRuleAdjuster(var step: Double) : CrossDockRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockRule, value: Double): Double = roundTo(value, step)
}

/** Runs the cross dock rule steps in order and keeps a trace of each. */
data class CrossDockRuleEngine(
    var steps: MutableList<CrossDockRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockRule): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CrossDockRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CrossDockRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockRuleEngine(): CrossDockRuleEngine {
    return CrossDockRuleEngine(
        steps = mutableListOf<CrossDockRuleModifier>(
            PercentCrossDockRuleAdjuster(-24.2),
            NameCrossDockRuleAdjuster(mutableMapOf<String, Double>("south" to 1.16, "retail" to 1.22)),
            RoundingCrossDockRuleAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 45676.6,
    )
}
