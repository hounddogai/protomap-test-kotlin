package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.flags.Registry

/** One step of the clearance snapshot calculation. */
interface ClearanceSnapshotFactor {
    fun describe(): String
    fun adjust(item: ClearanceSnapshot, value: Double): Double
}

data class ClearanceSnapshotScoreOffsetAdjuster(var weight: Double) : ClearanceSnapshotFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ClearanceSnapshot, value: Double): Double = value + item.score * weight
}

data class TitleClearanceSnapshotAdjuster(var factors: MutableMap<String, Double>) : ClearanceSnapshotFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ClearanceSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedClearanceSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredClearanceSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance snapshot steps in order and keeps a trace of each. */
data class ClearanceSnapshotEngine(
    var steps: MutableList<ClearanceSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceSnapshotEngine(flags: Registry): ClearanceSnapshotEngine {
    return ClearanceSnapshotEngine(
        steps = mutableListOf<ClearanceSnapshotFactor>(
            ClearanceSnapshotScoreOffsetAdjuster(1.39),
            TitleClearanceSnapshotAdjuster(mutableMapOf<String, Double>("retail" to 1.33, "east" to 1.3)),
            FlaggedClearanceSnapshotAdjuster(flags, "clearance-snapshot-weighted", 2.7),
            TieredClearanceSnapshotAdjuster(
                mutableListOf<Double>(2061.0, 4047.0),
                mutableListOf<Double>(1.06, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 30171.9,
    )
}
