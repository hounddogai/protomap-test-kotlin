package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted defect segment calculation. */
interface WeightedDefectSegmentStep {
    fun describe(): String
    fun adjust(item: WeightedDefectSegment, value: Double): Double
}

data class CapWeightedDefectSegmentAdjuster(var cap: Double) : WeightedDefectSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedDefectSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedDefectSegmentAdjuster(var step: Double) : WeightedDefectSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedDefectSegment, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedDefectSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDefectSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDefectSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedDefectSegmentWindowDaysOffsetAdjuster(var weight: Double) : WeightedDefectSegmentStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: WeightedDefectSegment, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class RegionWeightedDefectSegmentAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDefectSegmentStep {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedDefectSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted defect segment steps in order and keeps a trace of each. */
data class WeightedDefectSegmentEngine(
    var steps: MutableList<WeightedDefectSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedDefectSegment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDefectSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDefectSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedDefectSegmentEngine(flags: Registry): WeightedDefectSegmentEngine {
    return WeightedDefectSegmentEngine(
        steps = mutableListOf<WeightedDefectSegmentStep>(
            CapWeightedDefectSegmentAdjuster(8075.0),
            RoundingWeightedDefectSegmentAdjuster(1.0),
            FlaggedWeightedDefectSegmentAdjuster(flags, "weighted-defect-segment-pending", 3.7),
            WeightedDefectSegmentWindowDaysOffsetAdjuster(0.11),
            RegionWeightedDefectSegmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.93, "east" to 0.7, "central" to 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 74574.5,
    )
}
