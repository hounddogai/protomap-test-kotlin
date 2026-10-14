package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.flags.Registry

/** One step of the roster snapshot calculation. */
interface RosterSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: RosterSnapshot, value: Double): Double
}

data class TieredRosterSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RosterSnapshotRetriesOffsetAdjuster(var weight: Double) : RosterSnapshotAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RosterSnapshot, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedRosterSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the roster snapshot steps in order and keeps a trace of each. */
data class RosterSnapshotEngine(
    var steps: MutableList<RosterSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RosterSnapshot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RosterSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRosterSnapshotEngine(flags: Registry): RosterSnapshotEngine {
    return RosterSnapshotEngine(
        steps = mutableListOf<RosterSnapshotAdjuster>(
            TieredRosterSnapshotAdjuster(
                mutableListOf<Double>(728.0, 8158.0, 8734.0),
                mutableListOf<Double>(1.03, 0.96, 1.0),
            ),
            RosterSnapshotRetriesOffsetAdjuster(0.88),
            FlaggedRosterSnapshotAdjuster(flags, "roster-snapshot-primary", -8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 83014.5,
    )
}
