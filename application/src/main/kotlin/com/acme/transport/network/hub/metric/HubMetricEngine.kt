package com.acme.transport.network.hub.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub metric calculation. */
interface HubMetricStep {
    fun describe(): String
    fun adjust(item: HubMetric, value: Double): Double
}

data class HubMetricPriorityOffsetAdjuster(var weight: Double) : HubMetricStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: HubMetric, value: Double): Double = value + item.priority.toDouble() * weight
}

data class RoundingHubMetricAdjuster(var step: Double) : HubMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubMetric, value: Double): Double = roundTo(value, step)
}

data class PercentHubMetricAdjuster(var percent: Double) : HubMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumHubMetricAdjuster(var minimum: Double) : HubMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleHubMetricAdjuster(var factors: MutableMap<String, Double>) : HubMetricStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: HubMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHubMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hub metric steps in order and keeps a trace of each. */
data class HubMetricEngine(
    var steps: MutableList<HubMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HubMetric): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHubMetricEngine(flags: Registry): HubMetricEngine {
    return HubMetricEngine(
        steps = mutableListOf<HubMetricStep>(
            HubMetricPriorityOffsetAdjuster(0.84),
            RoundingHubMetricAdjuster(0.05),
            PercentHubMetricAdjuster(-0.7),
            MinimumHubMetricAdjuster(21.2),
            LocaleHubMetricAdjuster(mutableMapOf<String, Double>("south" to 0.87, "east" to 0.99)),
            FlaggedHubMetricAdjuster(flags, "hub-metric-adaptive", -2.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 22341.1,
    )
}
