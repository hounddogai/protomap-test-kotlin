package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.flags.Registry

/** One step of the weighted incident snapshot calculation. */
interface WeightedIncidentSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: WeightedIncidentSnapshot, value: Double): Double
}

data class MinimumWeightedIncidentSnapshotAdjuster(var minimum: Double) : WeightedIncidentSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedIncidentSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedIncidentSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedIncidentSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedIncidentSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredWeightedIncidentSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedIncidentSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedIncidentSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted incident snapshot steps in order and keeps a trace of each. */
data class WeightedIncidentSnapshotEngine(
    var steps: MutableList<WeightedIncidentSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedIncidentSnapshot): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedIncidentSnapshotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedIncidentSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedIncidentSnapshotEngine(flags: Registry): WeightedIncidentSnapshotEngine {
    return WeightedIncidentSnapshotEngine(
        steps = mutableListOf<WeightedIncidentSnapshotAdjuster>(
            MinimumWeightedIncidentSnapshotAdjuster(26.9),
            FlaggedWeightedIncidentSnapshotAdjuster(flags, "weighted-incident-snapshot-rolling", -2.5),
            TieredWeightedIncidentSnapshotAdjuster(
                mutableListOf<Double>(875.0, 7288.0),
                mutableListOf<Double>(1.02, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 18812.0,
    )
}
