package com.acme.transport.safety.scorecard.snapshot

/** One step of the scheduled scorecard snapshot calculation. */
interface ScheduledScorecardSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledScorecardSnapshot, value: Double): Double
}

data class ScheduledScorecardSnapshotRatioOffsetAdjuster(
    var weight: Double,
) : ScheduledScorecardSnapshotAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ScheduledScorecardSnapshot, value: Double): Double = value + item.ratio * weight
}

data class TieredScheduledScorecardSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledScorecardSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledScorecardSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledScorecardSnapshotAdjuster(var cap: Double) : ScheduledScorecardSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledScorecardSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled scorecard snapshot steps in order and keeps a trace of each. */
data class ScheduledScorecardSnapshotEngine(
    var steps: MutableList<ScheduledScorecardSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledScorecardSnapshot): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledScorecardSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledScorecardSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledScorecardSnapshotEngine(): ScheduledScorecardSnapshotEngine {
    return ScheduledScorecardSnapshotEngine(
        steps = mutableListOf<ScheduledScorecardSnapshotAdjuster>(
            ScheduledScorecardSnapshotRatioOffsetAdjuster(1.95),
            TieredScheduledScorecardSnapshotAdjuster(
                mutableListOf<Double>(5189.0, 7695.0, 7991.0),
                mutableListOf<Double>(1.02, 0.85, 0.84),
            ),
            CapScheduledScorecardSnapshotAdjuster(17611.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 64620.2,
    )
}
