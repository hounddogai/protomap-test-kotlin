package com.acme.transport.freightpay.ratecard.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the rate card adjustment calculation. */
interface RateCardAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: RateCardAdjustment, value: Double): Double
}

data class FlaggedRateCardAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRateCardAdjustmentAdjuster(var cap: Double) : RateCardAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardAdjustment, value: Double): Double = minOf(value, cap)
}

data class PercentRateCardAdjustmentAdjuster(var percent: Double) : RateCardAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RateCardAdjustmentCeilingOffsetAdjuster(var weight: Double) : RateCardAdjustmentAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RateCardAdjustment, value: Double): Double = value + item.ceiling * weight
}

/** Runs the rate card adjustment steps in order and keeps a trace of each. */
data class RateCardAdjustmentEngine(
    var steps: MutableList<RateCardAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardAdjustment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardAdjustmentEngine(flags: Registry): RateCardAdjustmentEngine {
    return RateCardAdjustmentEngine(
        steps = mutableListOf<RateCardAdjustmentAdjuster>(
            FlaggedRateCardAdjustmentAdjuster(flags, "rate-card-adjustment-global", -8.6),
            CapRateCardAdjustmentAdjuster(1257.0),
            PercentRateCardAdjustmentAdjuster(-20.0),
            RateCardAdjustmentCeilingOffsetAdjuster(1.19),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 98149.7,
    )
}
