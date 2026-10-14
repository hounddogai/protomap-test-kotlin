package com.acme.transport.freightpay.dispute.disputetemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual dispute snapshot calculation. */
interface ManualDisputeSnapshotFactor {
    fun describe(): String
    fun adjust(item: ManualDisputeSnapshot, value: Double): Double
}

data class CapManualDisputeSnapshotAdjuster(var cap: Double) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double = minOf(value, cap)
}

data class UnitManualDisputeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualDisputeSnapshotAdjuster(var minimum: Double) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualDisputeSnapshotDurationMinutesOffsetAdjuster(
    var weight: Double,
) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class FlaggedManualDisputeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualDisputeSnapshotAdjuster(var step: Double) : ManualDisputeSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDisputeSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the manual dispute snapshot steps in order and keeps a trace of each. */
data class ManualDisputeSnapshotEngine(
    var steps: MutableList<ManualDisputeSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDisputeSnapshot): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDisputeSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDisputeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDisputeSnapshotEngine(flags: Registry): ManualDisputeSnapshotEngine {
    return ManualDisputeSnapshotEngine(
        steps = mutableListOf<ManualDisputeSnapshotFactor>(
            CapManualDisputeSnapshotAdjuster(4366.0),
            UnitManualDisputeSnapshotAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
            MinimumManualDisputeSnapshotAdjuster(9.7),
            ManualDisputeSnapshotDurationMinutesOffsetAdjuster(0.04),
            FlaggedManualDisputeSnapshotAdjuster(flags, "manual-dispute-snapshot-fallback", 0.8),
            RoundingManualDisputeSnapshotAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 67519.6,
    )
}
