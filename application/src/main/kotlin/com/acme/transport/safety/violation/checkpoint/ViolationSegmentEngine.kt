package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation segment calculation. */
interface ViolationSegmentModifier {
    fun describe(): String
    fun adjust(item: ViolationSegment, value: Double): Double
}

data class ViolationSegmentMarginOffsetAdjuster(var weight: Double) : ViolationSegmentModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ViolationSegment, value: Double): Double = value + item.margin * weight
}

data class RoundingViolationSegmentAdjuster(var step: Double) : ViolationSegmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationSegment, value: Double): Double = roundTo(value, step)
}

data class MinimumViolationSegmentAdjuster(var minimum: Double) : ViolationSegmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleViolationSegmentAdjuster(var factors: MutableMap<String, Double>) : ViolationSegmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ViolationSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentViolationSegmentAdjuster(var percent: Double) : ViolationSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the violation segment steps in order and keeps a trace of each. */
data class ViolationSegmentEngine(
    var steps: MutableList<ViolationSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationSegment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationSegmentEngine(): ViolationSegmentEngine {
    return ViolationSegmentEngine(
        steps = mutableListOf<ViolationSegmentModifier>(
            ViolationSegmentMarginOffsetAdjuster(1.28),
            RoundingViolationSegmentAdjuster(5.0),
            MinimumViolationSegmentAdjuster(23.5),
            TitleViolationSegmentAdjuster(mutableMapOf<String, Double>("north" to 0.86)),
            PercentViolationSegmentAdjuster(-10.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 14849.9,
    )
}
