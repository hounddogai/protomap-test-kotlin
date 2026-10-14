package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual defect rollup calculation. */
interface ManualDefectRollupFactor {
    fun describe(): String
    fun adjust(item: ManualDefectRollup, value: Double): Double
}

data class RoundingManualDefectRollupAdjuster(var step: Double) : ManualDefectRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDefectRollup, value: Double): Double = roundTo(value, step)
}

data class CapManualDefectRollupAdjuster(var cap: Double) : ManualDefectRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDefectRollup, value: Double): Double = minOf(value, cap)
}

data class SegmentManualDefectRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDefectRollupFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualDefectRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualDefectRollupAdjuster(var percent: Double) : ManualDefectRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualDefectRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualDefectRollupGraceDaysOffsetAdjuster(var weight: Double) : ManualDefectRollupFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ManualDefectRollup, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumManualDefectRollupAdjuster(var minimum: Double) : ManualDefectRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDefectRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual defect rollup steps in order and keeps a trace of each. */
data class ManualDefectRollupEngine(
    var steps: MutableList<ManualDefectRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualDefectRollup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDefectRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDefectRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualDefectRollupEngine(): ManualDefectRollupEngine {
    return ManualDefectRollupEngine(
        steps = mutableListOf<ManualDefectRollupFactor>(
            RoundingManualDefectRollupAdjuster(0.25),
            CapManualDefectRollupAdjuster(20728.0),
            SegmentManualDefectRollupAdjuster(mutableMapOf<String, Double>("central" to 1.33)),
            PercentManualDefectRollupAdjuster(-23.3),
            ManualDefectRollupGraceDaysOffsetAdjuster(0.85),
            MinimumManualDefectRollupAdjuster(24.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 20119.5,
    )
}
