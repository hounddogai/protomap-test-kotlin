package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.flags.Registry

/** One step of the rate card segment calculation. */
interface RateCardSegmentModifier {
    fun describe(): String
    fun adjust(item: RateCardSegment, value: Double): Double
}

data class FlaggedRateCardSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardSegmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRateCardSegmentAdjuster(var cap: Double) : RateCardSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardSegment, value: Double): Double = minOf(value, cap)
}

data class RateCardSegmentBudgetOffsetAdjuster(var weight: Double) : RateCardSegmentModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RateCardSegment, value: Double): Double = value + item.budget * weight
}

/** Runs the rate card segment steps in order and keeps a trace of each. */
data class RateCardSegmentEngine(
    var steps: MutableList<RateCardSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardSegmentEngine(flags: Registry): RateCardSegmentEngine {
    return RateCardSegmentEngine(
        steps = mutableListOf<RateCardSegmentModifier>(
            FlaggedRateCardSegmentAdjuster(flags, "rate-card-segment-lenient", 7.1),
            CapRateCardSegmentAdjuster(45608.0),
            RateCardSegmentBudgetOffsetAdjuster(1.29),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 78088.3,
    )
}
