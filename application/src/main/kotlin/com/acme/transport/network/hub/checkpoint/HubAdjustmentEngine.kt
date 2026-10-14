package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub adjustment calculation. */
interface HubAdjustmentStep {
    fun describe(): String
    fun adjust(item: HubAdjustment, value: Double): Double
}

data class PercentHubAdjustmentAdjuster(var percent: Double) : HubAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedHubAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubAdjustmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingHubAdjustmentAdjuster(var step: Double) : HubAdjustmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubAdjustment, value: Double): Double = roundTo(value, step)
}

data class TieredHubAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub adjustment steps in order and keeps a trace of each. */
data class HubAdjustmentEngine(
    var steps: MutableList<HubAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubAdjustment): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubAdjustmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<HubAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubAdjustmentEngine(flags: Registry): HubAdjustmentEngine {
    return HubAdjustmentEngine(
        steps = mutableListOf<HubAdjustmentStep>(
            PercentHubAdjustmentAdjuster(-20.4),
            FlaggedHubAdjustmentAdjuster(flags, "hub-adjustment-seasonal", -0.8),
            RoundingHubAdjustmentAdjuster(1.0),
            TieredHubAdjustmentAdjuster(
                mutableListOf<Double>(3806.0, 9273.0, 9455.0),
                mutableListOf<Double>(1.18, 1.19, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 81512.3,
    )
}
