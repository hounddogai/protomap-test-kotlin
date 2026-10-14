package com.acme.transport.network.hub.batch

import com.acme.transport.kit.flags.Registry

/** One step of the hub baseline calculation. */
interface HubBaselineStep {
    fun describe(): String
    fun adjust(item: HubBaseline, value: Double): Double
}

data class FlaggedHubBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHubBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHubBaselineAdjuster(var percent: Double) : HubBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleHubBaselineAdjuster(var factors: MutableMap<String, Double>) : HubBaselineStep {
    override fun describe(): String = "by title"

    override fun adjust(item: HubBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumHubBaselineAdjuster(var minimum: Double) : HubBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hub baseline steps in order and keeps a trace of each. */
data class HubBaselineEngine(
    var steps: MutableList<HubBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubBaselineEngine(flags: Registry): HubBaselineEngine {
    return HubBaselineEngine(
        steps = mutableListOf<HubBaselineStep>(
            FlaggedHubBaselineAdjuster(flags, "hub-baseline-primary", 5.6),
            TieredHubBaselineAdjuster(
                mutableListOf<Double>(895.0, 3206.0, 4419.0, 7225.0),
                mutableListOf<Double>(0.88, 1.07, 1.15, 1.18),
            ),
            PercentHubBaselineAdjuster(-9.7),
            TitleHubBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 0.79, "retail" to 1.37, "central" to 0.97),
            ),
            MinimumHubBaselineAdjuster(8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 91377.2,
    )
}
