package com.acme.transport.network.cutoff.snapshot

/** One step of the strict cutoff snapshot calculation. */
interface StrictCutoffSnapshotStep {
    fun describe(): String
    fun adjust(item: StrictCutoffSnapshot, value: Double): Double
}

data class StrictCutoffSnapshotCapacityOffsetAdjuster(var weight: Double) : StrictCutoffSnapshotStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: StrictCutoffSnapshot, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredStrictCutoffSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCutoffSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCutoffSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictCutoffSnapshotAdjuster(var cap: Double) : StrictCutoffSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCutoffSnapshot, value: Double): Double = minOf(value, cap)
}

data class LocaleStrictCutoffSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCutoffSnapshotStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: StrictCutoffSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictCutoffSnapshotAdjuster(var percent: Double) : StrictCutoffSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCutoffSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict cutoff snapshot steps in order and keeps a trace of each. */
data class StrictCutoffSnapshotEngine(
    var steps: MutableList<StrictCutoffSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictCutoffSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictCutoffSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictCutoffSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictCutoffSnapshotEngine(): StrictCutoffSnapshotEngine {
    return StrictCutoffSnapshotEngine(
        steps = mutableListOf<StrictCutoffSnapshotStep>(
            StrictCutoffSnapshotCapacityOffsetAdjuster(0.56),
            TieredStrictCutoffSnapshotAdjuster(
                mutableListOf<Double>(905.0, 1248.0, 3359.0, 7379.0),
                mutableListOf<Double>(1.16, 0.95, 1.15, 1.12),
            ),
            CapStrictCutoffSnapshotAdjuster(16106.0),
            LocaleStrictCutoffSnapshotAdjuster(mutableMapOf<String, Double>("south" to 1.32, "online" to 1.22)),
            PercentStrictCutoffSnapshotAdjuster(-1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 4296.8,
    )
}
