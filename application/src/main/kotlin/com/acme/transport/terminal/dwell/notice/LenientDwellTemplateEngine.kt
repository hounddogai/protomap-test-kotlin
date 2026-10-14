package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient dwell template calculation. */
interface LenientDwellTemplateStep {
    fun describe(): String
    fun adjust(item: LenientDwellTemplate, value: Double): Double
}

data class TieredLenientDwellTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientDwellTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientDwellTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientDwellTemplateAdjuster(var percent: Double) : LenientDwellTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientDwellTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientDwellTemplateAdjuster(var cap: Double) : LenientDwellTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDwellTemplate, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientDwellTemplateAdjuster(var minimum: Double) : LenientDwellTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDwellTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient dwell template steps in order and keeps a trace of each. */
data class LenientDwellTemplateEngine(
    var steps: MutableList<LenientDwellTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientDwellTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientDwellTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientDwellTemplateEngine(): LenientDwellTemplateEngine {
    return LenientDwellTemplateEngine(
        steps = mutableListOf<LenientDwellTemplateStep>(
            TieredLenientDwellTemplateAdjuster(
                mutableListOf<Double>(2830.0, 8813.0),
                mutableListOf<Double>(1.11, 1.0),
            ),
            PercentLenientDwellTemplateAdjuster(-5.0),
            CapLenientDwellTemplateAdjuster(4439.0),
            MinimumLenientDwellTemplateAdjuster(26.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 63498.0,
    )
}
