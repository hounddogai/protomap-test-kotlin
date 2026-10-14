package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual camera event summary calculation. */
interface ManualCameraEventSummaryStep {
    fun describe(): String
    fun adjust(item: ManualCameraEventSummary, value: Double): Double
}

data class PercentManualCameraEventSummaryAdjuster(var percent: Double) : ManualCameraEventSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCameraEventSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameManualCameraEventSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualCameraEventSummaryStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualCameraEventSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualCameraEventSummaryAdjuster(var step: Double) : ManualCameraEventSummaryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCameraEventSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the manual camera event summary steps in order and keeps a trace of each. */
data class ManualCameraEventSummaryEngine(
    var steps: MutableList<ManualCameraEventSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualCameraEventSummary): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCameraEventSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCameraEventSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualCameraEventSummaryEngine(): ManualCameraEventSummaryEngine {
    return ManualCameraEventSummaryEngine(
        steps = mutableListOf<ManualCameraEventSummaryStep>(
            PercentManualCameraEventSummaryAdjuster(-21.0),
            NameManualCameraEventSummaryAdjuster(
                mutableMapOf<String, Double>("west" to 0.71, "central" to 1.03),
            ),
            RoundingManualCameraEventSummaryAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 2145.6,
    )
}
