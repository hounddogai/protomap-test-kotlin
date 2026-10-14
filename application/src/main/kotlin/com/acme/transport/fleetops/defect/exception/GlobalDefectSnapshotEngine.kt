package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the global defect snapshot calculation. */
interface GlobalDefectSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: GlobalDefectSnapshot, value: Double): Double
}

data class GlobalDefectSnapshotLoadKgOffsetAdjuster(var weight: Double) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double = value + item.loadKg * weight
}

data class RoundingGlobalDefectSnapshotAdjuster(var step: Double) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalDefectSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalDefectSnapshotAdjuster(var percent: Double) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameGlobalDefectSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalDefectSnapshotAdjuster(var minimum: Double) : GlobalDefectSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDefectSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global defect snapshot steps in order and keeps a trace of each. */
data class GlobalDefectSnapshotEngine(
    var steps: MutableList<GlobalDefectSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalDefectSnapshot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalDefectSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalDefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalDefectSnapshotEngine(): GlobalDefectSnapshotEngine {
    return GlobalDefectSnapshotEngine(
        steps = mutableListOf<GlobalDefectSnapshotAdjuster>(
            GlobalDefectSnapshotLoadKgOffsetAdjuster(0.2),
            RoundingGlobalDefectSnapshotAdjuster(0.5),
            TieredGlobalDefectSnapshotAdjuster(
                mutableListOf<Double>(2020.0, 4373.0, 8581.0, 9037.0),
                mutableListOf<Double>(0.9, 0.92, 1.0, 0.94),
            ),
            PercentGlobalDefectSnapshotAdjuster(-15.5),
            NameGlobalDefectSnapshotAdjuster(mutableMapOf<String, Double>("west" to 1.23, "south" to 0.84)),
            MinimumGlobalDefectSnapshotAdjuster(35.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 55473.9,
    )
}
