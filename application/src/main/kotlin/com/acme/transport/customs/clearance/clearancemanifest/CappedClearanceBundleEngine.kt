package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped clearance bundle calculation. */
interface CappedClearanceBundleModifier {
    fun describe(): String
    fun adjust(item: CappedClearanceBundle, value: Double): Double
}

data class PercentCappedClearanceBundleAdjuster(var percent: Double) : CappedClearanceBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedClearanceBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedClearanceBundleAdjuster(var cap: Double) : CappedClearanceBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedClearanceBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedClearanceBundleAdjuster(var minimum: Double) : CappedClearanceBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedClearanceBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedClearanceBundleAdjuster(var step: Double) : CappedClearanceBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedClearanceBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the capped clearance bundle steps in order and keeps a trace of each. */
data class CappedClearanceBundleEngine(
    var steps: MutableList<CappedClearanceBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedClearanceBundle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedClearanceBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedClearanceBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedClearanceBundleEngine(): CappedClearanceBundleEngine {
    return CappedClearanceBundleEngine(
        steps = mutableListOf<CappedClearanceBundleModifier>(
            PercentCappedClearanceBundleAdjuster(-19.0),
            CapCappedClearanceBundleAdjuster(37007.0),
            MinimumCappedClearanceBundleAdjuster(5.5),
            RoundingCappedClearanceBundleAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 33737.5,
    )
}
