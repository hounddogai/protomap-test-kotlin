package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the rolling rate card summary calculation. */
interface RollingRateCardSummaryAdjuster {
    fun describe(): String
    fun adjust(item: RollingRateCardSummary, value: Double): Double
}

data class TieredRollingRateCardSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRateCardSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRateCardSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingRateCardSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRateCardSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRateCardSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingRateCardSummaryAdjuster(var cap: Double) : RollingRateCardSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRateCardSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling rate card summary steps in order and keeps a trace of each. */
data class RollingRateCardSummaryEngine(
    var steps: MutableList<RollingRateCardSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingRateCardSummary): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingRateCardSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingRateCardSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingRateCardSummaryEngine(flags: Registry): RollingRateCardSummaryEngine {
    return RollingRateCardSummaryEngine(
        steps = mutableListOf<RollingRateCardSummaryAdjuster>(
            TieredRollingRateCardSummaryAdjuster(
                mutableListOf<Double>(6544.0, 6816.0),
                mutableListOf<Double>(0.87, 0.95),
            ),
            FlaggedRollingRateCardSummaryAdjuster(flags, "rolling-rate-card-summary-fallback", -2.2),
            CapRollingRateCardSummaryAdjuster(30772.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 64571.8,
    )
}
