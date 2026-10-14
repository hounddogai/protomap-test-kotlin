package com.acme.transport.network.node.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual node policy calculation. */
interface ManualNodePolicyModifier {
    fun describe(): String
    fun adjust(item: ManualNodePolicy, value: Double): Double
}

data class MinimumManualNodePolicyAdjuster(var minimum: Double) : ManualNodePolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualNodePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualNodePolicyAdjuster(var step: Double) : ManualNodePolicyModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualNodePolicy, value: Double): Double = roundTo(value, step)
}

data class TieredManualNodePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualNodePolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualNodePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual node policy steps in order and keeps a trace of each. */
data class ManualNodePolicyEngine(
    var steps: MutableList<ManualNodePolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualNodePolicy): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualNodePolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualNodePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualNodePolicyEngine(): ManualNodePolicyEngine {
    return ManualNodePolicyEngine(
        steps = mutableListOf<ManualNodePolicyModifier>(
            MinimumManualNodePolicyAdjuster(7.6),
            RoundingManualNodePolicyAdjuster(0.05),
            TieredManualNodePolicyAdjuster(
                mutableListOf<Double>(2197.0, 3477.0, 9159.0),
                mutableListOf<Double>(0.94, 0.83, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 31386.2,
    )
}
