package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the incident rule calculation. */
interface IncidentRuleModifier {
    fun describe(): String
    fun adjust(item: IncidentRule, value: Double): Double
}

data class RoundingIncidentRuleAdjuster(var step: Double) : IncidentRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentRule, value: Double): Double = roundTo(value, step)
}

data class SegmentIncidentRuleAdjuster(var factors: MutableMap<String, Double>) : IncidentRuleModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: IncidentRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumIncidentRuleAdjuster(var minimum: Double) : IncidentRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class IncidentRuleSequenceOffsetAdjuster(var weight: Double) : IncidentRuleModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: IncidentRule, value: Double): Double = value + item.sequence.toDouble() * weight
}

/** Runs the incident rule steps in order and keeps a trace of each. */
data class IncidentRuleEngine(
    var steps: MutableList<IncidentRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: IncidentRule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IncidentRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<IncidentRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultIncidentRuleEngine(): IncidentRuleEngine {
    return IncidentRuleEngine(
        steps = mutableListOf<IncidentRuleModifier>(
            RoundingIncidentRuleAdjuster(0.25),
            SegmentIncidentRuleAdjuster(mutableMapOf<String, Double>("west" to 1.01)),
            MinimumIncidentRuleAdjuster(25.5),
            IncidentRuleSequenceOffsetAdjuster(1.98),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 24504.9,
    )
}
