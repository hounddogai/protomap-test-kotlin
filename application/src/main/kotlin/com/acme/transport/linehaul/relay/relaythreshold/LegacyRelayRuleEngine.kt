package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy relay rule calculation. */
interface LegacyRelayRuleModifier {
    fun describe(): String
    fun adjust(item: LegacyRelayRule, value: Double): Double
}

data class LaneLegacyRelayRuleAdjuster(var factors: MutableMap<String, Double>) : LegacyRelayRuleModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyRelayRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLegacyRelayRuleAdjuster(var step: Double) : LegacyRelayRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyRelayRule, value: Double): Double = roundTo(value, step)
}

data class LegacyRelayRulePriorityOffsetAdjuster(var weight: Double) : LegacyRelayRuleModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LegacyRelayRule, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the legacy relay rule steps in order and keeps a trace of each. */
data class LegacyRelayRuleEngine(
    var steps: MutableList<LegacyRelayRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyRelayRule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyRelayRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyRelayRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyRelayRuleEngine(): LegacyRelayRuleEngine {
    return LegacyRelayRuleEngine(
        steps = mutableListOf<LegacyRelayRuleModifier>(
            LaneLegacyRelayRuleAdjuster(mutableMapOf<String, Double>("south" to 1.06, "online" to 1.36)),
            RoundingLegacyRelayRuleAdjuster(0.1),
            LegacyRelayRulePriorityOffsetAdjuster(1.26),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 11587.4,
    )
}
