package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.flags.Registry

/** One step of the cutoff snapshot calculation. */
interface CutoffSnapshotModifier {
    fun describe(): String
    fun adjust(item: CutoffSnapshot, value: Double): Double
}

data class TitleCutoffSnapshotAdjuster(var factors: MutableMap<String, Double>) : CutoffSnapshotModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: CutoffSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCutoffSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCutoffSnapshotAdjuster(var percent: Double) : CutoffSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cutoff snapshot steps in order and keeps a trace of each. */
data class CutoffSnapshotEngine(
    var steps: MutableList<CutoffSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CutoffSnapshot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCutoffSnapshotEngine(flags: Registry): CutoffSnapshotEngine {
    return CutoffSnapshotEngine(
        steps = mutableListOf<CutoffSnapshotModifier>(
            TitleCutoffSnapshotAdjuster(mutableMapOf<String, Double>("west" to 1.26)),
            FlaggedCutoffSnapshotAdjuster(flags, "cutoff-snapshot-rolling", 5.9),
            PercentCutoffSnapshotAdjuster(6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 88572.5,
    )
}
