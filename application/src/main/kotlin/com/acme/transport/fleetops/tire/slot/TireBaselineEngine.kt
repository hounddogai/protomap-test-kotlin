package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tire baseline calculation. */
interface TireBaselineModifier {
    fun describe(): String
    fun adjust(item: TireBaseline, value: Double): Double
}

data class PercentTireBaselineAdjuster(var percent: Double) : TireBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTireBaselineAdjuster(var cap: Double) : TireBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredTireBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TireBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TireBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tire baseline steps in order and keeps a trace of each. */
data class TireBaselineEngine(
    var steps: MutableList<TireBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TireBaseline): Double {
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

    fun evaluateAll(items: MutableList<TireBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTireBaselineEngine(): TireBaselineEngine {
    return TireBaselineEngine(
        steps = mutableListOf<TireBaselineModifier>(
            PercentTireBaselineAdjuster(-18.6),
            CapTireBaselineAdjuster(28966.0),
            TieredTireBaselineAdjuster(mutableListOf<Double>(1911.0, 4333.0), mutableListOf<Double>(0.84, 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 41899.9,
    )
}
