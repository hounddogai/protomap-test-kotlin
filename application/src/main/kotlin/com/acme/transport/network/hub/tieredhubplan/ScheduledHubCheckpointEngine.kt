package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled hub checkpoint calculation. */
interface ScheduledHubCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledHubCheckpoint, value: Double): Double
}

data class NameScheduledHubCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledHubCheckpointAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledHubCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledHubCheckpointAdjuster(var cap: Double) : ScheduledHubCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledHubCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledHubCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHubCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHubCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledHubCheckpointSequenceOffsetAdjuster(var weight: Double) : ScheduledHubCheckpointAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: ScheduledHubCheckpoint, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class PercentScheduledHubCheckpointAdjuster(var percent: Double) : ScheduledHubCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHubCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled hub checkpoint steps in order and keeps a trace of each. */
data class ScheduledHubCheckpointEngine(
    var steps: MutableList<ScheduledHubCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledHubCheckpoint): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledHubCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledHubCheckpointEngine(flags: Registry): ScheduledHubCheckpointEngine {
    return ScheduledHubCheckpointEngine(
        steps = mutableListOf<ScheduledHubCheckpointAdjuster>(
            NameScheduledHubCheckpointAdjuster(mutableMapOf<String, Double>("central" to 1.25)),
            CapScheduledHubCheckpointAdjuster(25221.0),
            FlaggedScheduledHubCheckpointAdjuster(flags, "scheduled-hub-checkpoint-seasonal", -2.7),
            ScheduledHubCheckpointSequenceOffsetAdjuster(2.0),
            PercentScheduledHubCheckpointAdjuster(21.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 69650.2,
    )
}
