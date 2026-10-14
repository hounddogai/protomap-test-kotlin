package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual dwell segment calculation. */
interface ManualDwellSegmentStep {
    fun describe(): String
    fun adjust(item: ManualDwellSegment, value: Double): Double
}

data class CapManualDwellSegmentAdjuster(var cap: Double) : ManualDwellSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDwellSegment, value: Double): Double = minOf(value, cap)
}

data class RoundingManualDwellSegmentAdjuster(var step: Double) : ManualDwellSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDwellSegment, value: Double): Double = roundTo(value, step)
}

data class MinimumManualDwellSegmentAdjuster(var minimum: Double) : ManualDwellSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDwellSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual dwell segment steps in order and keeps a trace of each. */
data class ManualDwellSegmentEngine(
    var steps: MutableList<ManualDwellSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDwellSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDwellSegmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDwellSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDwellSegmentEngine(): ManualDwellSegmentEngine {
    return ManualDwellSegmentEngine(
        steps = mutableListOf<ManualDwellSegmentStep>(
            CapManualDwellSegmentAdjuster(34449.0),
            RoundingManualDwellSegmentAdjuster(0.05),
            MinimumManualDwellSegmentAdjuster(31.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 77879.1,
    )
}
