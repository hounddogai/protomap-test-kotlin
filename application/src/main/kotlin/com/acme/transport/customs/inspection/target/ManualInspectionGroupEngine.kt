package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual inspection group calculation. */
interface ManualInspectionGroupAdjuster {
    fun describe(): String
    fun adjust(item: ManualInspectionGroup, value: Double): Double
}

data class RoundingManualInspectionGroupAdjuster(var step: Double) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double = roundTo(value, step)
}

data class SegmentManualInspectionGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualInspectionGroupAdjuster(var cap: Double) : ManualInspectionGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualInspectionGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the manual inspection group steps in order and keeps a trace of each. */
data class ManualInspectionGroupEngine(
    var steps: MutableList<ManualInspectionGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualInspectionGroup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualInspectionGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualInspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualInspectionGroupEngine(): ManualInspectionGroupEngine {
    return ManualInspectionGroupEngine(
        steps = mutableListOf<ManualInspectionGroupAdjuster>(
            RoundingManualInspectionGroupAdjuster(0.1),
            SegmentManualInspectionGroupAdjuster(mutableMapOf<String, Double>("east" to 0.84)),
            CapManualInspectionGroupAdjuster(36696.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 45014.9,
    )
}
