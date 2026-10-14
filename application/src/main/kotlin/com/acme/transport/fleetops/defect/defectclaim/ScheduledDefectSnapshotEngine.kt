package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled defect snapshot calculation. */
interface ScheduledDefectSnapshotFactor {
    fun describe(): String
    fun adjust(item: ScheduledDefectSnapshot, value: Double): Double
}

data class RoundingScheduledDefectSnapshotAdjuster(var step: Double) : ScheduledDefectSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledDefectSnapshot, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledDefectSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDefectSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDefectSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ScheduledDefectSnapshotExpiresAtOffsetAdjuster(var weight: Double) : ScheduledDefectSnapshotFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ScheduledDefectSnapshot, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

/** Runs the scheduled defect snapshot steps in order and keeps a trace of each. */
data class ScheduledDefectSnapshotEngine(
    var steps: MutableList<ScheduledDefectSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledDefectSnapshot): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDefectSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledDefectSnapshotEngine(flags: Registry): ScheduledDefectSnapshotEngine {
    return ScheduledDefectSnapshotEngine(
        steps = mutableListOf<ScheduledDefectSnapshotFactor>(
            RoundingScheduledDefectSnapshotAdjuster(0.5),
            FlaggedScheduledDefectSnapshotAdjuster(flags, "scheduled-defect-snapshot-global", 0.3),
            ScheduledDefectSnapshotExpiresAtOffsetAdjuster(1.75),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 1350.0,
    )
}
