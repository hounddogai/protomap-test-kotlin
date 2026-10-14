package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the strict idling snapshot calculation. */
interface StrictIdlingSnapshotStep {
    fun describe(): String
    fun adjust(item: StrictIdlingSnapshot, value: Double): Double
}

data class StrictIdlingSnapshotScoreOffsetAdjuster(var weight: Double) : StrictIdlingSnapshotStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: StrictIdlingSnapshot, value: Double): Double = value + item.score * weight
}

data class FlaggedStrictIdlingSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIdlingSnapshotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIdlingSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictIdlingSnapshotAdjuster(var minimum: Double) : StrictIdlingSnapshotStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictIdlingSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict idling snapshot steps in order and keeps a trace of each. */
data class StrictIdlingSnapshotEngine(
    var steps: MutableList<StrictIdlingSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictIdlingSnapshot): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictIdlingSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictIdlingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictIdlingSnapshotEngine(flags: Registry): StrictIdlingSnapshotEngine {
    return StrictIdlingSnapshotEngine(
        steps = mutableListOf<StrictIdlingSnapshotStep>(
            StrictIdlingSnapshotScoreOffsetAdjuster(1.18),
            FlaggedStrictIdlingSnapshotAdjuster(flags, "strict-idling-snapshot-weighted", 2.6),
            MinimumStrictIdlingSnapshotAdjuster(43.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 8465.0,
    )
}
