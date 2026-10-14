package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted transit time digest calculation. */
interface WeightedTransitTimeDigestAdjuster {
    fun describe(): String
    fun adjust(item: WeightedTransitTimeDigest, value: Double): Double
}

data class FlaggedWeightedTransitTimeDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedTransitTimeDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedTransitTimeDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingWeightedTransitTimeDigestAdjuster(var step: Double) : WeightedTransitTimeDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTransitTimeDigest, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedTransitTimeDigestAdjuster(var percent: Double) : WeightedTransitTimeDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTransitTimeDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedTransitTimeDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTransitTimeDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTransitTimeDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted transit time digest steps in order and keeps a trace of each. */
data class WeightedTransitTimeDigestEngine(
    var steps: MutableList<WeightedTransitTimeDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedTransitTimeDigest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTransitTimeDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTransitTimeDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedTransitTimeDigestEngine(flags: Registry): WeightedTransitTimeDigestEngine {
    return WeightedTransitTimeDigestEngine(
        steps = mutableListOf<WeightedTransitTimeDigestAdjuster>(
            FlaggedWeightedTransitTimeDigestAdjuster(flags, "weighted-transit-time-digest-rolling", -3.5),
            RoundingWeightedTransitTimeDigestAdjuster(0.1),
            PercentWeightedTransitTimeDigestAdjuster(-11.2),
            TieredWeightedTransitTimeDigestAdjuster(
                mutableListOf<Double>(5131.0, 5973.0),
                mutableListOf<Double>(0.88, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 51194.3,
    )
}
