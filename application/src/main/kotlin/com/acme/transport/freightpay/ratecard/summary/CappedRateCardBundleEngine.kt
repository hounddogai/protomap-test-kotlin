package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped rate card bundle calculation. */
interface CappedRateCardBundleAdjuster {
    fun describe(): String
    fun adjust(item: CappedRateCardBundle, value: Double): Double
}

data class CategoryCappedRateCardBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRateCardBundleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedRateCardBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCappedRateCardBundleAdjuster(var step: Double) : CappedRateCardBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRateCardBundle, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedRateCardBundleAdjuster(var minimum: Double) : CappedRateCardBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRateCardBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped rate card bundle steps in order and keeps a trace of each. */
data class CappedRateCardBundleEngine(
    var steps: MutableList<CappedRateCardBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedRateCardBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRateCardBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRateCardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedRateCardBundleEngine(): CappedRateCardBundleEngine {
    return CappedRateCardBundleEngine(
        steps = mutableListOf<CappedRateCardBundleAdjuster>(
            CategoryCappedRateCardBundleAdjuster(mutableMapOf<String, Double>("online" to 1.36)),
            RoundingCappedRateCardBundleAdjuster(0.05),
            MinimumCappedRateCardBundleAdjuster(8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 31717.9,
    )
}
