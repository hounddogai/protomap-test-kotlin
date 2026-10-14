package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient service area window calculation. */
interface LenientServiceAreaWindowModifier {
    fun describe(): String
    fun adjust(item: LenientServiceAreaWindow, value: Double): Double
}

data class PercentLenientServiceAreaWindowAdjuster(var percent: Double) : LenientServiceAreaWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientServiceAreaWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLenientServiceAreaWindowAdjuster(var cap: Double) : LenientServiceAreaWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientServiceAreaWindow, value: Double): Double = minOf(value, cap)
}

data class LenientServiceAreaWindowEndsAtOffsetAdjuster(var weight: Double) : LenientServiceAreaWindowModifier {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LenientServiceAreaWindow, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

data class CountryLenientServiceAreaWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServiceAreaWindowModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientServiceAreaWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientServiceAreaWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientServiceAreaWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientServiceAreaWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient service area window steps in order and keeps a trace of each. */
data class LenientServiceAreaWindowEngine(
    var steps: MutableList<LenientServiceAreaWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientServiceAreaWindow): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientServiceAreaWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientServiceAreaWindowEngine(): LenientServiceAreaWindowEngine {
    return LenientServiceAreaWindowEngine(
        steps = mutableListOf<LenientServiceAreaWindowModifier>(
            PercentLenientServiceAreaWindowAdjuster(-16.6),
            CapLenientServiceAreaWindowAdjuster(28856.0),
            LenientServiceAreaWindowEndsAtOffsetAdjuster(0.98),
            CountryLenientServiceAreaWindowAdjuster(mutableMapOf<String, Double>("online" to 0.86)),
            TieredLenientServiceAreaWindowAdjuster(
                mutableListOf<Double>(5203.0, 5871.0),
                mutableListOf<Double>(1.02, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 19666.4,
    )
}
