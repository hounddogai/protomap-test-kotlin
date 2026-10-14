package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual trailer yard assignment calculation. */
interface ManualTrailerYardAssignmentFactor {
    fun describe(): String
    fun adjust(item: ManualTrailerYardAssignment, value: Double): Double
}

data class PercentManualTrailerYardAssignmentAdjuster(var percent: Double) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualTrailerYardAssignmentAdjuster(var step: Double) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double = roundTo(value, step)
}

data class CapManualTrailerYardAssignmentAdjuster(var cap: Double) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double = minOf(value, cap)
}

data class ManualTrailerYardAssignmentMinimumOffsetAdjuster(
    var weight: Double,
) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double = value + item.minimum * weight
}

data class TieredManualTrailerYardAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualTrailerYardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualTrailerYardAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualTrailerYardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual trailer yard assignment steps in order and keeps a trace of each. */
data class ManualTrailerYardAssignmentEngine(
    var steps: MutableList<ManualTrailerYardAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTrailerYardAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTrailerYardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTrailerYardAssignmentEngine(flags: Registry): ManualTrailerYardAssignmentEngine {
    return ManualTrailerYardAssignmentEngine(
        steps = mutableListOf<ManualTrailerYardAssignmentFactor>(
            PercentManualTrailerYardAssignmentAdjuster(-7.5),
            RoundingManualTrailerYardAssignmentAdjuster(1.0),
            CapManualTrailerYardAssignmentAdjuster(3764.0),
            ManualTrailerYardAssignmentMinimumOffsetAdjuster(1.73),
            TieredManualTrailerYardAssignmentAdjuster(
                mutableListOf<Double>(1568.0, 2609.0, 8036.0, 9627.0),
                mutableListOf<Double>(0.85, 1.1, 0.91, 1.03),
            ),
            FlaggedManualTrailerYardAssignmentAdjuster(flags, "manual-trailer-yard-assignment-pending", -5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 87538.9,
    )
}
