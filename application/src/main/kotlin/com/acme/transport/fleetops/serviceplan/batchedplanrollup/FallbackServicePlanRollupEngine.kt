package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.flags.Registry

/** One step of the fallback service plan rollup calculation. */
interface FallbackServicePlanRollupFactor {
    fun describe(): String
    fun adjust(item: FallbackServicePlanRollup, value: Double): Double
}

data class PercentFallbackServicePlanRollupAdjuster(var percent: Double) : FallbackServicePlanRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackServicePlanRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackServicePlanRollupAdjuster(var minimum: Double) : FallbackServicePlanRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackServicePlanRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFallbackServicePlanRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackServicePlanRollupFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackServicePlanRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackServicePlanRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackServicePlanRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackServicePlanRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FallbackServicePlanRollupWindowDaysOffsetAdjuster(
    var weight: Double,
) : FallbackServicePlanRollupFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: FallbackServicePlanRollup, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the fallback service plan rollup steps in order and keeps a trace of each. */
data class FallbackServicePlanRollupEngine(
    var steps: MutableList<FallbackServicePlanRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackServicePlanRollup): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackServicePlanRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackServicePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackServicePlanRollupEngine(flags: Registry): FallbackServicePlanRollupEngine {
    return FallbackServicePlanRollupEngine(
        steps = mutableListOf<FallbackServicePlanRollupFactor>(
            PercentFallbackServicePlanRollupAdjuster(22.8),
            MinimumFallbackServicePlanRollupAdjuster(4.4),
            LabelFallbackServicePlanRollupAdjuster(mutableMapOf<String, Double>("online" to 1.3)),
            FlaggedFallbackServicePlanRollupAdjuster(flags, "fallback-service-plan-rollup-batched", 9.4),
            FallbackServicePlanRollupWindowDaysOffsetAdjuster(1.21),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 49811.9,
    )
}
