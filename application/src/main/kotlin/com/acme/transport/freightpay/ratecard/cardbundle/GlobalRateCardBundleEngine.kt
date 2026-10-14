package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global rate card bundle calculation. */
interface GlobalRateCardBundleStep {
    fun describe(): String
    fun adjust(item: GlobalRateCardBundle, value: Double): Double
}

data class GlobalRateCardBundleThresholdOffsetAdjuster(var weight: Double) : GlobalRateCardBundleStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: GlobalRateCardBundle, value: Double): Double = value + item.threshold * weight
}

data class FlaggedGlobalRateCardBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRateCardBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRateCardBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentGlobalRateCardBundleAdjuster(var percent: Double) : GlobalRateCardBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRateCardBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingGlobalRateCardBundleAdjuster(var step: Double) : GlobalRateCardBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRateCardBundle, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalRateCardBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRateCardBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRateCardBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global rate card bundle steps in order and keeps a trace of each. */
data class GlobalRateCardBundleEngine(
    var steps: MutableList<GlobalRateCardBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalRateCardBundle): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRateCardBundleStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRateCardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalRateCardBundleEngine(flags: Registry): GlobalRateCardBundleEngine {
    return GlobalRateCardBundleEngine(
        steps = mutableListOf<GlobalRateCardBundleStep>(
            GlobalRateCardBundleThresholdOffsetAdjuster(1.9),
            FlaggedGlobalRateCardBundleAdjuster(flags, "global-rate-card-bundle-primary", 2.0),
            PercentGlobalRateCardBundleAdjuster(3.8),
            RoundingGlobalRateCardBundleAdjuster(0.05),
            TieredGlobalRateCardBundleAdjuster(
                mutableListOf<Double>(1875.0, 4919.0, 7343.0, 9909.0),
                mutableListOf<Double>(0.89, 1.12, 0.9, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 93824.0,
    )
}
