package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card slot calculation. */
interface RateCardSlotStep {
    fun describe(): String
    fun adjust(item: RateCardSlot, value: Double): Double
}

data class RoundingRateCardSlotAdjuster(var step: Double) : RateCardSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardSlot, value: Double): Double = roundTo(value, step)
}

data class MinimumRateCardSlotAdjuster(var minimum: Double) : RateCardSlotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRateCardSlotAdjuster(var percent: Double) : RateCardSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LocaleRateCardSlotAdjuster(var factors: MutableMap<String, Double>) : RateCardSlotStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: RateCardSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRateCardSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card slot steps in order and keeps a trace of each. */
data class RateCardSlotEngine(
    var steps: MutableList<RateCardSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardSlot): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardSlotEngine(): RateCardSlotEngine {
    return RateCardSlotEngine(
        steps = mutableListOf<RateCardSlotStep>(
            RoundingRateCardSlotAdjuster(5.0),
            MinimumRateCardSlotAdjuster(9.0),
            PercentRateCardSlotAdjuster(8.3),
            LocaleRateCardSlotAdjuster(mutableMapOf<String, Double>("online" to 1.39, "central" to 0.75)),
            TieredRateCardSlotAdjuster(
                mutableListOf<Double>(1127.0, 3111.0, 3367.0, 6808.0),
                mutableListOf<Double>(1.06, 0.97, 0.93, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 45901.2,
    )
}
