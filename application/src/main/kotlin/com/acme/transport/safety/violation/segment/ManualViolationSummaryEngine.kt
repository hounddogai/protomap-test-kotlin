package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual violation summary calculation. */
interface ManualViolationSummaryModifier {
    fun describe(): String
    fun adjust(item: ManualViolationSummary, value: Double): Double
}

data class RoundingManualViolationSummaryAdjuster(var step: Double) : ManualViolationSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualViolationSummary, value: Double): Double = roundTo(value, step)
}

data class ManualViolationSummaryMaximumOffsetAdjuster(var weight: Double) : ManualViolationSummaryModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ManualViolationSummary, value: Double): Double = value + item.maximum * weight
}

data class PercentManualViolationSummaryAdjuster(var percent: Double) : ManualViolationSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualViolationSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualViolationSummaryAdjuster(var cap: Double) : ManualViolationSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualViolationSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumManualViolationSummaryAdjuster(var minimum: Double) : ManualViolationSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualViolationSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual violation summary steps in order and keeps a trace of each. */
data class ManualViolationSummaryEngine(
    var steps: MutableList<ManualViolationSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualViolationSummary): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualViolationSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualViolationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualViolationSummaryEngine(): ManualViolationSummaryEngine {
    return ManualViolationSummaryEngine(
        steps = mutableListOf<ManualViolationSummaryModifier>(
            RoundingManualViolationSummaryAdjuster(0.5),
            ManualViolationSummaryMaximumOffsetAdjuster(1.8),
            PercentManualViolationSummaryAdjuster(20.2),
            CapManualViolationSummaryAdjuster(9578.0),
            MinimumManualViolationSummaryAdjuster(36.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 92791.6,
    )
}
