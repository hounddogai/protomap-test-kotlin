package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted rate card threshold calculation. */
interface WeightedRateCardThresholdStep {
    fun describe(): String
    fun adjust(item: WeightedRateCardThreshold, value: Double): Double
}

data class TieredWeightedRateCardThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRateCardThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRateCardThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedRateCardThresholdAdjuster(var minimum: Double) : WeightedRateCardThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRateCardThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedRateCardThresholdAdjuster(var percent: Double) : WeightedRateCardThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRateCardThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedRateCardThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRateCardThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRateCardThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedRateCardThresholdAdjuster(var cap: Double) : WeightedRateCardThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRateCardThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted rate card threshold steps in order and keeps a trace of each. */
data class WeightedRateCardThresholdEngine(
    var steps: MutableList<WeightedRateCardThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedRateCardThreshold): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRateCardThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedRateCardThresholdEngine(flags: Registry): WeightedRateCardThresholdEngine {
    return WeightedRateCardThresholdEngine(
        steps = mutableListOf<WeightedRateCardThresholdStep>(
            TieredWeightedRateCardThresholdAdjuster(
                mutableListOf<Double>(1396.0, 4905.0, 9155.0),
                mutableListOf<Double>(0.94, 0.93, 1.16),
            ),
            MinimumWeightedRateCardThresholdAdjuster(1.4),
            PercentWeightedRateCardThresholdAdjuster(-2.3),
            FlaggedWeightedRateCardThresholdAdjuster(flags, "weighted-rate-card-threshold-strict", 1.2),
            CapWeightedRateCardThresholdAdjuster(40308.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 88012.5,
    )
}
