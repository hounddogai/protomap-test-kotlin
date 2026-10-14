package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the fallback backhaul milestone calculation. */
interface FallbackBackhaulMilestoneFactor {
    fun describe(): String
    fun adjust(item: FallbackBackhaulMilestone, value: Double): Double
}

data class TieredFallbackBackhaulMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackBackhaulMilestoneFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackBackhaulMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackBackhaulMilestoneAdjuster(var percent: Double) : FallbackBackhaulMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackBackhaulMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackBackhaulMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackBackhaulMilestoneFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackBackhaulMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback backhaul milestone steps in order and keeps a trace of each. */
data class FallbackBackhaulMilestoneEngine(
    var steps: MutableList<FallbackBackhaulMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackBackhaulMilestone): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackBackhaulMilestoneFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackBackhaulMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackBackhaulMilestoneEngine(flags: Registry): FallbackBackhaulMilestoneEngine {
    return FallbackBackhaulMilestoneEngine(
        steps = mutableListOf<FallbackBackhaulMilestoneFactor>(
            TieredFallbackBackhaulMilestoneAdjuster(
                mutableListOf<Double>(327.0, 1425.0, 1844.0, 2170.0),
                mutableListOf<Double>(0.98, 1.14, 1.07, 0.84),
            ),
            PercentFallbackBackhaulMilestoneAdjuster(-18.7),
            FlaggedFallbackBackhaulMilestoneAdjuster(flags, "fallback-backhaul-milestone-adaptive", -4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 13335.1,
    )
}
