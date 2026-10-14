package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual rest break profile calculation. */
interface ManualRestBreakProfileStep {
    fun describe(): String
    fun adjust(item: ManualRestBreakProfile, value: Double): Double
}

data class PercentManualRestBreakProfileAdjuster(var percent: Double) : ManualRestBreakProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualRestBreakProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualRestBreakProfileAdjuster(var step: Double) : ManualRestBreakProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRestBreakProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumManualRestBreakProfileAdjuster(var minimum: Double) : ManualRestBreakProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRestBreakProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual rest break profile steps in order and keeps a trace of each. */
data class ManualRestBreakProfileEngine(
    var steps: MutableList<ManualRestBreakProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualRestBreakProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualRestBreakProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualRestBreakProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualRestBreakProfileEngine(): ManualRestBreakProfileEngine {
    return ManualRestBreakProfileEngine(
        steps = mutableListOf<ManualRestBreakProfileStep>(
            PercentManualRestBreakProfileAdjuster(-8.3),
            RoundingManualRestBreakProfileAdjuster(0.05),
            MinimumManualRestBreakProfileAdjuster(34.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 71349.9,
    )
}
