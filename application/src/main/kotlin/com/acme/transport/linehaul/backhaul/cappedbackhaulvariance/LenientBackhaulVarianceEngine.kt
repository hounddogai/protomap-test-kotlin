package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient backhaul variance calculation. */
interface LenientBackhaulVarianceFactor {
    fun describe(): String
    fun adjust(item: LenientBackhaulVariance, value: Double): Double
}

data class CapLenientBackhaulVarianceAdjuster(var cap: Double) : LenientBackhaulVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientBackhaulVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientBackhaulVarianceAdjuster(var step: Double) : LenientBackhaulVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientBackhaulVariance, value: Double): Double = roundTo(value, step)
}

data class CountryLenientBackhaulVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientBackhaulVarianceFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientBackhaulVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient backhaul variance steps in order and keeps a trace of each. */
data class LenientBackhaulVarianceEngine(
    var steps: MutableList<LenientBackhaulVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientBackhaulVariance): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientBackhaulVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientBackhaulVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientBackhaulVarianceEngine(): LenientBackhaulVarianceEngine {
    return LenientBackhaulVarianceEngine(
        steps = mutableListOf<LenientBackhaulVarianceFactor>(
            CapLenientBackhaulVarianceAdjuster(37759.0),
            RoundingLenientBackhaulVarianceAdjuster(0.1),
            CountryLenientBackhaulVarianceAdjuster(mutableMapOf<String, Double>("west" to 1.39)),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 95687.4,
    )
}
