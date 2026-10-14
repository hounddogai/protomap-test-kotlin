package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.flags.Registry

/** One step of the weighted defect snapshot calculation. */
interface WeightedDefectSnapshotStep {
    fun describe(): String
    fun adjust(item: WeightedDefectSnapshot, value: Double): Double
}

data class TieredWeightedDefectSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDefectSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDefectSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedDefectSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDefectSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDefectSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedDefectSnapshotRetriesOffsetAdjuster(var weight: Double) : WeightedDefectSnapshotStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: WeightedDefectSnapshot, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

/** Runs the weighted defect snapshot steps in order and keeps a trace of each. */
data class WeightedDefectSnapshotEngine(
    var steps: MutableList<WeightedDefectSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDefectSnapshot): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDefectSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDefectSnapshotEngine(flags: Registry): WeightedDefectSnapshotEngine {
    return WeightedDefectSnapshotEngine(
        steps = mutableListOf<WeightedDefectSnapshotStep>(
            TieredWeightedDefectSnapshotAdjuster(
                mutableListOf<Double>(5328.0, 8794.0),
                mutableListOf<Double>(0.91, 0.92),
            ),
            FlaggedWeightedDefectSnapshotAdjuster(flags, "weighted-defect-snapshot-lenient", 4.3),
            WeightedDefectSnapshotRetriesOffsetAdjuster(1.41),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 55134.1,
    )
}
