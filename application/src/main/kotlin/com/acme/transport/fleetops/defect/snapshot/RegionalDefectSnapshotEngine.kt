package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional defect snapshot calculation. */
interface RegionalDefectSnapshotModifier {
    fun describe(): String
    fun adjust(item: RegionalDefectSnapshot, value: Double): Double
}

data class RegionRegionalDefectSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalDefectSnapshotAdjuster(var cap: Double) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double = minOf(value, cap)
}

data class RegionalDefectSnapshotWindowDaysOffsetAdjuster(var weight: Double) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumRegionalDefectSnapshotAdjuster(var minimum: Double) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingRegionalDefectSnapshotAdjuster(var step: Double) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalDefectSnapshotAdjuster(var percent: Double) : RegionalDefectSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDefectSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional defect snapshot steps in order and keeps a trace of each. */
data class RegionalDefectSnapshotEngine(
    var steps: MutableList<RegionalDefectSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalDefectSnapshot): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDefectSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDefectSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalDefectSnapshotEngine(): RegionalDefectSnapshotEngine {
    return RegionalDefectSnapshotEngine(
        steps = mutableListOf<RegionalDefectSnapshotModifier>(
            RegionRegionalDefectSnapshotAdjuster(
                mutableMapOf<String, Double>("central" to 0.82, "east" to 1.16),
            ),
            CapRegionalDefectSnapshotAdjuster(10380.0),
            RegionalDefectSnapshotWindowDaysOffsetAdjuster(1.62),
            MinimumRegionalDefectSnapshotAdjuster(16.6),
            RoundingRegionalDefectSnapshotAdjuster(0.5),
            PercentRegionalDefectSnapshotAdjuster(-1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 18126.5,
    )
}
