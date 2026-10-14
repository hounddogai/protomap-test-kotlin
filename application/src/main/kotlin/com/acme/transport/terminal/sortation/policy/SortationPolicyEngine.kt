package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the sortation policy calculation. */
interface SortationPolicyFactor {
    fun describe(): String
    fun adjust(item: SortationPolicy, value: Double): Double
}

data class FlaggedSortationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSortationPolicyAdjuster(var step: Double) : SortationPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentSortationPolicyAdjuster(var percent: Double) : SortationPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the sortation policy steps in order and keeps a trace of each. */
data class SortationPolicyEngine(
    var steps: MutableList<SortationPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SortationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationPolicyEngine(flags: Registry): SortationPolicyEngine {
    return SortationPolicyEngine(
        steps = mutableListOf<SortationPolicyFactor>(
            FlaggedSortationPolicyAdjuster(flags, "sortation-policy-capped", -6.2),
            RoundingSortationPolicyAdjuster(1.0),
            PercentSortationPolicyAdjuster(7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 58394.4,
    )
}
