package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary cross dock policy calculation. */
interface PrimaryCrossDockPolicyFactor {
    fun describe(): String
    fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double
}

data class DepotPrimaryCrossDockPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryCrossDockPolicyPriorityOffsetAdjuster(var weight: Double) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentPrimaryCrossDockPolicyAdjuster(var percent: Double) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryCrossDockPolicyAdjuster(var cap: Double) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryCrossDockPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryCrossDockPolicyAdjuster(var step: Double) : PrimaryCrossDockPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCrossDockPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the primary cross dock policy steps in order and keeps a trace of each. */
data class PrimaryCrossDockPolicyEngine(
    var steps: MutableList<PrimaryCrossDockPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryCrossDockPolicy): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryCrossDockPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryCrossDockPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryCrossDockPolicyEngine(): PrimaryCrossDockPolicyEngine {
    return PrimaryCrossDockPolicyEngine(
        steps = mutableListOf<PrimaryCrossDockPolicyFactor>(
            DepotPrimaryCrossDockPolicyAdjuster(
                mutableMapOf<String, Double>("central" to 0.71, "north" to 1.23),
            ),
            PrimaryCrossDockPolicyPriorityOffsetAdjuster(1.63),
            PercentPrimaryCrossDockPolicyAdjuster(-24.3),
            CapPrimaryCrossDockPolicyAdjuster(49392.0),
            TieredPrimaryCrossDockPolicyAdjuster(
                mutableListOf<Double>(288.0, 1999.0, 5187.0, 8397.0),
                mutableListOf<Double>(1.11, 1.11, 0.89, 1.17),
            ),
            RoundingPrimaryCrossDockPolicyAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 16564.0,
    )
}
