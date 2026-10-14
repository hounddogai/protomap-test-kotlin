package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled dwell snapshot calculation. */
interface ScheduledDwellSnapshotFactor {
    fun describe(): String
    fun adjust(item: ScheduledDwellSnapshot, value: Double): Double
}

data class TieredScheduledDwellSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDwellSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDwellSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumScheduledDwellSnapshotAdjuster(var minimum: Double) : ScheduledDwellSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledDwellSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledDwellSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDwellSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDwellSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UnitScheduledDwellSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDwellSnapshotFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledDwellSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled dwell snapshot steps in order and keeps a trace of each. */
data class ScheduledDwellSnapshotEngine(
    var steps: MutableList<ScheduledDwellSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDwellSnapshot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDwellSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDwellSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDwellSnapshotEngine(flags: Registry): ScheduledDwellSnapshotEngine {
    return ScheduledDwellSnapshotEngine(
        steps = mutableListOf<ScheduledDwellSnapshotFactor>(
            TieredScheduledDwellSnapshotAdjuster(
                mutableListOf<Double>(4911.0, 8264.0),
                mutableListOf<Double>(0.85, 0.85),
            ),
            MinimumScheduledDwellSnapshotAdjuster(5.9),
            FlaggedScheduledDwellSnapshotAdjuster(flags, "scheduled-dwell-snapshot-strict", -0.8),
            UnitScheduledDwellSnapshotAdjuster(mutableMapOf<String, Double>("west" to 1.26, "retail" to 1.03)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 22476.1,
    )
}
