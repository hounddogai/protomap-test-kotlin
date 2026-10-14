package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy swap notice calculation. */
interface LegacySwapNoticeModifier {
    fun describe(): String
    fun adjust(item: LegacySwapNotice, value: Double): Double
}

data class TieredLegacySwapNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacySwapNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacySwapNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacySwapNoticeAdjuster(var step: Double) : LegacySwapNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacySwapNotice, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacySwapNoticeAdjuster(var minimum: Double) : LegacySwapNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacySwapNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy swap notice steps in order and keeps a trace of each. */
data class LegacySwapNoticeEngine(
    var steps: MutableList<LegacySwapNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacySwapNotice): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacySwapNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacySwapNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacySwapNoticeEngine(): LegacySwapNoticeEngine {
    return LegacySwapNoticeEngine(
        steps = mutableListOf<LegacySwapNoticeModifier>(
            TieredLegacySwapNoticeAdjuster(
                mutableListOf<Double>(4648.0, 8959.0),
                mutableListOf<Double>(0.92, 0.82),
            ),
            RoundingLegacySwapNoticeAdjuster(0.1),
            MinimumLegacySwapNoticeAdjuster(11.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 21567.2,
    )
}
