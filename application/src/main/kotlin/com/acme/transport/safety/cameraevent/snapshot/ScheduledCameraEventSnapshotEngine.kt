package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled camera event snapshot calculation. */
interface ScheduledCameraEventSnapshotModifier {
    fun describe(): String
    fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double
}

data class MinimumScheduledCameraEventSnapshotAdjuster(
    var minimum: Double,
) : ScheduledCameraEventSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledCameraEventSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledCameraEventSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentScheduledCameraEventSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledCameraEventSnapshotModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledCameraEventSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledCameraEventSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledCameraEventSnapshotAdjuster(var cap: Double) : ScheduledCameraEventSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledCameraEventSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled camera event snapshot steps in order and keeps a trace of each. */
data class ScheduledCameraEventSnapshotEngine(
    var steps: MutableList<ScheduledCameraEventSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledCameraEventSnapshot): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledCameraEventSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledCameraEventSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledCameraEventSnapshotEngine(flags: Registry): ScheduledCameraEventSnapshotEngine {
    return ScheduledCameraEventSnapshotEngine(
        steps = mutableListOf<ScheduledCameraEventSnapshotModifier>(
            MinimumScheduledCameraEventSnapshotAdjuster(8.6),
            TieredScheduledCameraEventSnapshotAdjuster(
                mutableListOf<Double>(3100.0, 3441.0, 5950.0, 6372.0),
                mutableListOf<Double>(1.19, 1.08, 0.92, 0.83),
            ),
            SegmentScheduledCameraEventSnapshotAdjuster(mutableMapOf<String, Double>("retail" to 0.92)),
            FlaggedScheduledCameraEventSnapshotAdjuster(flags, "scheduled-camera-event-snapshot-capped", -0.1),
            CapScheduledCameraEventSnapshotAdjuster(45240.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 1764.1,
    )
}
