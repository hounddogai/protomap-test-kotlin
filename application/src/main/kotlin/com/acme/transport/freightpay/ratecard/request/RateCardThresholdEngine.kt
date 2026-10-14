package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.flags.Registry

/** One step of the rate card threshold calculation. */
interface RateCardThresholdAdjuster {
    fun describe(): String
    fun adjust(item: RateCardThreshold, value: Double): Double
}

data class DepotRateCardThresholdAdjuster(var factors: MutableMap<String, Double>) : RateCardThresholdAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: RateCardThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RateCardThresholdThresholdOffsetAdjuster(var weight: Double) : RateCardThresholdAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RateCardThreshold, value: Double): Double = value + item.threshold * weight
}

/** Runs the rate card threshold steps in order and keeps a trace of each. */
data class RateCardThresholdEngine(
    var steps: MutableList<RateCardThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardThresholdAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardThresholdEngine(flags: Registry): RateCardThresholdEngine {
    return RateCardThresholdEngine(
        steps = mutableListOf<RateCardThresholdAdjuster>(
            DepotRateCardThresholdAdjuster(
                mutableMapOf<String, Double>("retail" to 0.72, "south" to 1.05, "west" to 0.91),
            ),
            FlaggedRateCardThresholdAdjuster(flags, "rate-card-threshold-rolling", 9.1),
            RateCardThresholdThresholdOffsetAdjuster(1.59),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 19935.5,
    )
}
