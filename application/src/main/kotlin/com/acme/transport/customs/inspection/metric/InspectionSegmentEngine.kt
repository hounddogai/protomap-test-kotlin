package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection segment calculation. */
interface InspectionSegmentStep {
    fun describe(): String
    fun adjust(item: InspectionSegment, value: Double): Double
}

data class MinimumInspectionSegmentAdjuster(var minimum: Double) : InspectionSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingInspectionSegmentAdjuster(var step: Double) : InspectionSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionSegment, value: Double): Double = roundTo(value, step)
}

data class CapInspectionSegmentAdjuster(var cap: Double) : InspectionSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionSegment, value: Double): Double = minOf(value, cap)
}

data class InspectionSegmentBatchSizeOffsetAdjuster(var weight: Double) : InspectionSegmentStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: InspectionSegment, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the inspection segment steps in order and keeps a trace of each. */
data class InspectionSegmentEngine(
    var steps: MutableList<InspectionSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionSegment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionSegmentEngine(): InspectionSegmentEngine {
    return InspectionSegmentEngine(
        steps = mutableListOf<InspectionSegmentStep>(
            MinimumInspectionSegmentAdjuster(43.6),
            RoundingInspectionSegmentAdjuster(0.05),
            CapInspectionSegmentAdjuster(21455.0),
            InspectionSegmentBatchSizeOffsetAdjuster(1.67),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 17710.2,
    )
}
