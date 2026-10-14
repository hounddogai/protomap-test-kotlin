package com.acme.transport.fleetops.inspection.segment

/** One step of the fallback inspection segment calculation. */
interface FallbackInspectionSegmentModifier {
    fun describe(): String
    fun adjust(item: FallbackInspectionSegment, value: Double): Double
}

data class CapFallbackInspectionSegmentAdjuster(var cap: Double) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackInspectionSegmentAdjuster(var percent: Double) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackInspectionSegmentRevisionOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionSegmentModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: FallbackInspectionSegment, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

/** Runs the fallback inspection segment steps in order and keeps a trace of each. */
data class FallbackInspectionSegmentEngine(
    var steps: MutableList<FallbackInspectionSegmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackInspectionSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionSegmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackInspectionSegmentEngine(): FallbackInspectionSegmentEngine {
    return FallbackInspectionSegmentEngine(
        steps = mutableListOf<FallbackInspectionSegmentModifier>(
            CapFallbackInspectionSegmentAdjuster(45350.0),
            PercentFallbackInspectionSegmentAdjuster(-18.5),
            FallbackInspectionSegmentRevisionOffsetAdjuster(0.83),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 55247.2,
    )
}
