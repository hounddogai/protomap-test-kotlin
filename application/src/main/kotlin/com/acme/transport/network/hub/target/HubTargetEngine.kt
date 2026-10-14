package com.acme.transport.network.hub.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub target calculation. */
interface HubTargetModifier {
    fun describe(): String
    fun adjust(item: HubTarget, value: Double): Double
}

data class PercentHubTargetAdjuster(var percent: Double) : HubTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingHubTargetAdjuster(var step: Double) : HubTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumHubTargetAdjuster(var minimum: Double) : HubTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedHubTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapHubTargetAdjuster(var cap: Double) : HubTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubTarget, value: Double): Double = minOf(value, cap)
}

data class TieredHubTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub target steps in order and keeps a trace of each. */
data class HubTargetEngine(
    var steps: MutableList<HubTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HubTarget): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<HubTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHubTargetEngine(flags: Registry): HubTargetEngine {
    return HubTargetEngine(
        steps = mutableListOf<HubTargetModifier>(
            PercentHubTargetAdjuster(-12.0),
            RoundingHubTargetAdjuster(0.5),
            MinimumHubTargetAdjuster(1.6),
            FlaggedHubTargetAdjuster(flags, "hub-target-weighted", -9.8),
            CapHubTargetAdjuster(45389.0),
            TieredHubTargetAdjuster(
                mutableListOf<Double>(615.0, 6081.0, 8446.0),
                mutableListOf<Double>(1.07, 1.18, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 21162.8,
    )
}
