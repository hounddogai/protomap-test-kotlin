package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive sortation snapshot calculation. */
interface AdaptiveSortationSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveSortationSnapshot, value: Double): Double
}

data class DepotAdaptiveSortationSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveSortationSnapshotAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveSortationSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AdaptiveSortationSnapshotBudgetOffsetAdjuster(
    var weight: Double,
) : AdaptiveSortationSnapshotAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: AdaptiveSortationSnapshot, value: Double): Double = value + item.budget * weight
}

data class CapAdaptiveSortationSnapshotAdjuster(var cap: Double) : AdaptiveSortationSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSortationSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive sortation snapshot steps in order and keeps a trace of each. */
data class AdaptiveSortationSnapshotEngine(
    var steps: MutableList<AdaptiveSortationSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveSortationSnapshot): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveSortationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveSortationSnapshotEngine(): AdaptiveSortationSnapshotEngine {
    return AdaptiveSortationSnapshotEngine(
        steps = mutableListOf<AdaptiveSortationSnapshotAdjuster>(
            DepotAdaptiveSortationSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 1.09, "south" to 1.11, "online" to 1.1),
            ),
            AdaptiveSortationSnapshotBudgetOffsetAdjuster(0.84),
            CapAdaptiveSortationSnapshotAdjuster(19075.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 12807.9,
    )
}
