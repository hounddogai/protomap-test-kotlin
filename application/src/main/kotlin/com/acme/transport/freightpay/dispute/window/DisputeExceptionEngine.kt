package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute exception calculation. */
interface DisputeExceptionModifier {
    fun describe(): String
    fun adjust(item: DisputeException, value: Double): Double
}

data class TieredDisputeExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDisputeExceptionAdjuster(var percent: Double) : DisputeExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDisputeExceptionAdjuster(var cap: Double) : DisputeExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeException, value: Double): Double = minOf(value, cap)
}

data class RoundingDisputeExceptionAdjuster(var step: Double) : DisputeExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeException, value: Double): Double = roundTo(value, step)
}

data class DisputeExceptionPalletsOffsetAdjuster(var weight: Double) : DisputeExceptionModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: DisputeException, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the dispute exception steps in order and keeps a trace of each. */
data class DisputeExceptionEngine(
    var steps: MutableList<DisputeExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeException): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeExceptionEngine(): DisputeExceptionEngine {
    return DisputeExceptionEngine(
        steps = mutableListOf<DisputeExceptionModifier>(
            TieredDisputeExceptionAdjuster(
                mutableListOf<Double>(7131.0, 8952.0),
                mutableListOf<Double>(0.93, 1.05),
            ),
            PercentDisputeExceptionAdjuster(23.5),
            CapDisputeExceptionAdjuster(44809.0),
            RoundingDisputeExceptionAdjuster(5.0),
            DisputeExceptionPalletsOffsetAdjuster(1.86),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 34051.1,
    )
}
