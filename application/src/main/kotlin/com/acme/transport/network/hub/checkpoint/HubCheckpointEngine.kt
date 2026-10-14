package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hub checkpoint calculation. */
interface HubCheckpointModifier {
    fun describe(): String
    fun adjust(item: HubCheckpoint, value: Double): Double
}

data class MinimumHubCheckpointAdjuster(var minimum: Double) : HubCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HubCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingHubCheckpointAdjuster(var step: Double) : HubCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubCheckpoint, value: Double): Double = roundTo(value, step)
}

data class FlaggedHubCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubCheckpointModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentHubCheckpointAdjuster(var percent: Double) : HubCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the hub checkpoint steps in order and keeps a trace of each. */
data class HubCheckpointEngine(
    var steps: MutableList<HubCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubCheckpoint): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HubCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<HubCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubCheckpointEngine(flags: Registry): HubCheckpointEngine {
    return HubCheckpointEngine(
        steps = mutableListOf<HubCheckpointModifier>(
            MinimumHubCheckpointAdjuster(42.5),
            RoundingHubCheckpointAdjuster(1.0),
            FlaggedHubCheckpointAdjuster(flags, "hub-checkpoint-strict", -8.1),
            PercentHubCheckpointAdjuster(23.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 35402.7,
    )
}
