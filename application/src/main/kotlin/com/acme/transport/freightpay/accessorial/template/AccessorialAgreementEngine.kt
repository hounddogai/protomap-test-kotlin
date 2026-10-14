package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial agreement calculation. */
interface AccessorialAgreementStep {
    fun describe(): String
    fun adjust(item: AccessorialAgreement, value: Double): Double
}

data class TieredAccessorialAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneAccessorialAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialAgreementStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: AccessorialAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAccessorialAgreementAdjuster(var step: Double) : AccessorialAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialAgreement, value: Double): Double = roundTo(value, step)
}

data class AccessorialAgreementCapacityOffsetAdjuster(var weight: Double) : AccessorialAgreementStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AccessorialAgreement, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumAccessorialAgreementAdjuster(var minimum: Double) : AccessorialAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial agreement steps in order and keeps a trace of each. */
data class AccessorialAgreementEngine(
    var steps: MutableList<AccessorialAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AccessorialAgreement): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAccessorialAgreementEngine(): AccessorialAgreementEngine {
    return AccessorialAgreementEngine(
        steps = mutableListOf<AccessorialAgreementStep>(
            TieredAccessorialAgreementAdjuster(
                mutableListOf<Double>(1086.0, 4674.0, 9528.0),
                mutableListOf<Double>(1.07, 1.06, 0.86),
            ),
            LaneAccessorialAgreementAdjuster(mutableMapOf<String, Double>("retail" to 1.08)),
            RoundingAccessorialAgreementAdjuster(0.5),
            AccessorialAgreementCapacityOffsetAdjuster(0.78),
            MinimumAccessorialAgreementAdjuster(25.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 20509.5,
    )
}
