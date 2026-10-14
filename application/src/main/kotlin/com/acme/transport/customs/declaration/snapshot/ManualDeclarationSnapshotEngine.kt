package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the manual declaration snapshot calculation. */
interface ManualDeclarationSnapshotFactor {
    fun describe(): String
    fun adjust(item: ManualDeclarationSnapshot, value: Double): Double
}

data class TieredManualDeclarationSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDeclarationSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDeclarationSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualDeclarationSnapshotAdjuster(var percent: Double) : ManualDeclarationSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDeclarationSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedManualDeclarationSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDeclarationSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDeclarationSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ManualDeclarationSnapshotScoreOffsetAdjuster(var weight: Double) : ManualDeclarationSnapshotFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ManualDeclarationSnapshot, value: Double): Double = value + item.score * weight
}

/** Runs the manual declaration snapshot steps in order and keeps a trace of each. */
data class ManualDeclarationSnapshotEngine(
    var steps: MutableList<ManualDeclarationSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualDeclarationSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDeclarationSnapshotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDeclarationSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualDeclarationSnapshotEngine(flags: Registry): ManualDeclarationSnapshotEngine {
    return ManualDeclarationSnapshotEngine(
        steps = mutableListOf<ManualDeclarationSnapshotFactor>(
            TieredManualDeclarationSnapshotAdjuster(
                mutableListOf<Double>(3123.0, 4765.0, 5380.0),
                mutableListOf<Double>(0.84, 0.83, 1.0),
            ),
            PercentManualDeclarationSnapshotAdjuster(16.3),
            FlaggedManualDeclarationSnapshotAdjuster(flags, "manual-declaration-snapshot-scheduled", -6.7),
            ManualDeclarationSnapshotScoreOffsetAdjuster(1.11),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 41784.7,
    )
}
