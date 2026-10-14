package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.Registry

/** One step of the sortation snapshot calculation. */
interface SortationSnapshotStep {
    fun describe(): String
    fun adjust(item: SortationSnapshot, value: Double): Double
}

data class FlaggedSortationSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSortationSnapshotAdjuster(var percent: Double) : SortationSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSortationSnapshotAdjuster(var cap: Double) : SortationSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationSnapshot, value: Double): Double = minOf(value, cap)
}

data class MinimumSortationSnapshotAdjuster(var minimum: Double) : SortationSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneSortationSnapshotAdjuster(var factors: MutableMap<String, Double>) : SortationSnapshotStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: SortationSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SortationSnapshotUpdatedAtOffsetAdjuster(var weight: Double) : SortationSnapshotStep {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: SortationSnapshot, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the sortation snapshot steps in order and keeps a trace of each. */
data class SortationSnapshotEngine(
    var steps: MutableList<SortationSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationSnapshot): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<SortationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationSnapshotEngine(flags: Registry): SortationSnapshotEngine {
    return SortationSnapshotEngine(
        steps = mutableListOf<SortationSnapshotStep>(
            FlaggedSortationSnapshotAdjuster(flags, "sortation-snapshot-adaptive", -9.2),
            PercentSortationSnapshotAdjuster(-0.9),
            CapSortationSnapshotAdjuster(4589.0),
            MinimumSortationSnapshotAdjuster(4.0),
            LaneSortationSnapshotAdjuster(
                mutableMapOf<String, Double>("east" to 0.93, "north" to 1.13, "south" to 1.21),
            ),
            SortationSnapshotUpdatedAtOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 31834.7,
    )
}
