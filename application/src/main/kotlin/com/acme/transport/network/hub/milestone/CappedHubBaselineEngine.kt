package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped hub baseline calculation. */
interface CappedHubBaselineModifier {
    fun describe(): String
    fun adjust(item: CappedHubBaseline, value: Double): Double
}

data class CappedHubBaselineStartsAtOffsetAdjuster(var weight: Double) : CappedHubBaselineModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: CappedHubBaseline, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class MinimumCappedHubBaselineAdjuster(var minimum: Double) : CappedHubBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedHubBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedHubBaselineAdjuster(var percent: Double) : CappedHubBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedHubBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedHubBaselineAdjuster(var step: Double) : CappedHubBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHubBaseline, value: Double): Double = roundTo(value, step)
}

data class SourceCappedHubBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedHubBaselineModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedHubBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedHubBaselineAdjuster(var cap: Double) : CappedHubBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedHubBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the capped hub baseline steps in order and keeps a trace of each. */
data class CappedHubBaselineEngine(
    var steps: MutableList<CappedHubBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedHubBaseline): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedHubBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedHubBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedHubBaselineEngine(): CappedHubBaselineEngine {
    return CappedHubBaselineEngine(
        steps = mutableListOf<CappedHubBaselineModifier>(
            CappedHubBaselineStartsAtOffsetAdjuster(0.35),
            MinimumCappedHubBaselineAdjuster(49.5),
            PercentCappedHubBaselineAdjuster(-2.8),
            RoundingCappedHubBaselineAdjuster(0.25),
            SourceCappedHubBaselineAdjuster(mutableMapOf<String, Double>("west" to 1.01)),
            CapCappedHubBaselineAdjuster(11988.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 15302.2,
    )
}
