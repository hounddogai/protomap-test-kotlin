package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute notice calculation. */
interface DisputeNoticeAdjuster {
    fun describe(): String
    fun adjust(item: DisputeNotice, value: Double): Double
}

data class RoundingDisputeNoticeAdjuster(var step: Double) : DisputeNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeNotice, value: Double): Double = roundTo(value, step)
}

data class CapDisputeNoticeAdjuster(var cap: Double) : DisputeNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeNotice, value: Double): Double = minOf(value, cap)
}

data class MinimumDisputeNoticeAdjuster(var minimum: Double) : DisputeNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDisputeNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DisputeNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DisputeNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDisputeNoticeAdjuster(var percent: Double) : DisputeNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DisputeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DisputeNoticeWeightOffsetAdjuster(var weight: Double) : DisputeNoticeAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: DisputeNotice, value: Double): Double = value + item.weight * weight
}

/** Runs the dispute notice steps in order and keeps a trace of each. */
data class DisputeNoticeEngine(
    var steps: MutableList<DisputeNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DisputeNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDisputeNoticeEngine(): DisputeNoticeEngine {
    return DisputeNoticeEngine(
        steps = mutableListOf<DisputeNoticeAdjuster>(
            RoundingDisputeNoticeAdjuster(1.0),
            CapDisputeNoticeAdjuster(20660.0),
            MinimumDisputeNoticeAdjuster(49.1),
            TieredDisputeNoticeAdjuster(
                mutableListOf<Double>(1416.0, 6792.0, 8057.0, 8347.0),
                mutableListOf<Double>(0.97, 1.13, 0.86, 1.16),
            ),
            PercentDisputeNoticeAdjuster(-8.0),
            DisputeNoticeWeightOffsetAdjuster(0.16),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 96016.2,
    )
}
