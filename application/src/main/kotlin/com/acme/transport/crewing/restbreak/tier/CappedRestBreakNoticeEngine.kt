package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped rest break notice calculation. */
interface CappedRestBreakNoticeStep {
    fun describe(): String
    fun adjust(item: CappedRestBreakNotice, value: Double): Double
}

data class RoundingCappedRestBreakNoticeAdjuster(var step: Double) : CappedRestBreakNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRestBreakNotice, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedRestBreakNoticeAdjuster(var minimum: Double) : CappedRestBreakNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRestBreakNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedRestBreakNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRestBreakNoticeStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRestBreakNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped rest break notice steps in order and keeps a trace of each. */
data class CappedRestBreakNoticeEngine(
    var steps: MutableList<CappedRestBreakNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedRestBreakNotice): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedRestBreakNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedRestBreakNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedRestBreakNoticeEngine(): CappedRestBreakNoticeEngine {
    return CappedRestBreakNoticeEngine(
        steps = mutableListOf<CappedRestBreakNoticeStep>(
            RoundingCappedRestBreakNoticeAdjuster(0.05),
            MinimumCappedRestBreakNoticeAdjuster(49.7),
            TieredCappedRestBreakNoticeAdjuster(
                mutableListOf<Double>(5806.0, 6472.0),
                mutableListOf<Double>(0.83, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 66045.5,
    )
}
