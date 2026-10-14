package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled zone checkpoint calculation. */
interface ScheduledZoneCheckpointStep {
    fun describe(): String
    fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double
}

data class TieredScheduledZoneCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledZoneCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledZoneCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledZoneCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledZoneCheckpointAdjuster(var minimum: Double) : ScheduledZoneCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledZoneCheckpointDistanceKmOffsetAdjuster(var weight: Double) : ScheduledZoneCheckpointStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double = value + item.distanceKm * weight
}

data class NameScheduledZoneCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledZoneCheckpointStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledZoneCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled zone checkpoint steps in order and keeps a trace of each. */
data class ScheduledZoneCheckpointEngine(
    var steps: MutableList<ScheduledZoneCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledZoneCheckpoint): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledZoneCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledZoneCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledZoneCheckpointEngine(flags: Registry): ScheduledZoneCheckpointEngine {
    return ScheduledZoneCheckpointEngine(
        steps = mutableListOf<ScheduledZoneCheckpointStep>(
            TieredScheduledZoneCheckpointAdjuster(
                mutableListOf<Double>(55.0, 293.0, 3018.0, 6530.0),
                mutableListOf<Double>(1.08, 1.04, 1.1, 1.11),
            ),
            FlaggedScheduledZoneCheckpointAdjuster(flags, "scheduled-zone-checkpoint-capped", -2.8),
            MinimumScheduledZoneCheckpointAdjuster(10.3),
            ScheduledZoneCheckpointDistanceKmOffsetAdjuster(0.47),
            NameScheduledZoneCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 1.03, "west" to 1.11, "online" to 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 96347.4,
    )
}
