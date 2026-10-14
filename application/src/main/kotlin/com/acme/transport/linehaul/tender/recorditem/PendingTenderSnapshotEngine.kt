package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the pending tender snapshot calculation. */
interface PendingTenderSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: PendingTenderSnapshot, value: Double): Double
}

data class LabelPendingTenderSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingTenderSnapshotAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingTenderSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingTenderSnapshotAdjuster(var percent: Double) : PendingTenderSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTenderSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingTenderSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTenderSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTenderSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingTenderSnapshotAdjuster(var minimum: Double) : PendingTenderSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTenderSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPendingTenderSnapshotAdjuster(var cap: Double) : PendingTenderSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTenderSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the pending tender snapshot steps in order and keeps a trace of each. */
data class PendingTenderSnapshotEngine(
    var steps: MutableList<PendingTenderSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingTenderSnapshot): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTenderSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTenderSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingTenderSnapshotEngine(flags: Registry): PendingTenderSnapshotEngine {
    return PendingTenderSnapshotEngine(
        steps = mutableListOf<PendingTenderSnapshotAdjuster>(
            LabelPendingTenderSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 0.7, "north" to 1.33, "west" to 1.27),
            ),
            PercentPendingTenderSnapshotAdjuster(22.3),
            FlaggedPendingTenderSnapshotAdjuster(flags, "pending-tender-snapshot-batched", -0.5),
            MinimumPendingTenderSnapshotAdjuster(28.6),
            CapPendingTenderSnapshotAdjuster(44971.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 81476.6,
    )
}
