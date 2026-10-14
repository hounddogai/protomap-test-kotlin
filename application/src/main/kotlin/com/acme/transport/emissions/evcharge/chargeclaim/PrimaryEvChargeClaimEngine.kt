package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary ev charge claim calculation. */
interface PrimaryEvChargeClaimModifier {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeClaim, value: Double): Double
}

data class LabelPrimaryEvChargeClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryEvChargeClaimModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PrimaryEvChargeClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryEvChargeClaimAdjuster(var minimum: Double) : PrimaryEvChargeClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryEvChargeClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryEvChargeClaimAdjuster(var step: Double) : PrimaryEvChargeClaimModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEvChargeClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the primary ev charge claim steps in order and keeps a trace of each. */
data class PrimaryEvChargeClaimEngine(
    var steps: MutableList<PrimaryEvChargeClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryEvChargeClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryEvChargeClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryEvChargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeClaimEngine(): PrimaryEvChargeClaimEngine {
    return PrimaryEvChargeClaimEngine(
        steps = mutableListOf<PrimaryEvChargeClaimModifier>(
            LabelPrimaryEvChargeClaimAdjuster(mutableMapOf<String, Double>("west" to 1.38)),
            MinimumPrimaryEvChargeClaimAdjuster(26.7),
            RoundingPrimaryEvChargeClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 23318.3,
    )
}
