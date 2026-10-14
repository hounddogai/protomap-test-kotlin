package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card baseline calculation. */
interface RateCardBaselineStep {
    fun describe(): String
    fun adjust(item: RateCardBaseline, value: Double): Double
}

data class RateCardBaselineMarginOffsetAdjuster(var weight: Double) : RateCardBaselineStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RateCardBaseline, value: Double): Double = value + item.margin * weight
}

data class CapRateCardBaselineAdjuster(var cap: Double) : RateCardBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumRateCardBaselineAdjuster(var minimum: Double) : RateCardBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRateCardBaselineAdjuster(var percent: Double) : RateCardBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card baseline steps in order and keeps a trace of each. */
data class RateCardBaselineEngine(
    var steps: MutableList<RateCardBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardBaseline): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardBaselineEngine(): RateCardBaselineEngine {
    return RateCardBaselineEngine(
        steps = mutableListOf<RateCardBaselineStep>(
            RateCardBaselineMarginOffsetAdjuster(0.75),
            CapRateCardBaselineAdjuster(24142.0),
            MinimumRateCardBaselineAdjuster(8.6),
            PercentRateCardBaselineAdjuster(0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 60144.4,
    )
}
