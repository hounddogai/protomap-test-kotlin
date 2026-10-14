package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered parcel baseline calculation. */
interface TieredParcelBaselineAdjuster {
    fun describe(): String
    fun adjust(item: TieredParcelBaseline, value: Double): Double
}

data class PercentTieredParcelBaselineAdjuster(var percent: Double) : TieredParcelBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredParcelBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTieredParcelBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredParcelBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredParcelBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredParcelBaselineAdjuster(var cap: Double) : TieredParcelBaselineAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredParcelBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered parcel baseline steps in order and keeps a trace of each. */
data class TieredParcelBaselineEngine(
    var steps: MutableList<TieredParcelBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredParcelBaseline): Double {
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

    fun evaluateAll(items: MutableList<TieredParcelBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredParcelBaselineEngine(): TieredParcelBaselineEngine {
    return TieredParcelBaselineEngine(
        steps = mutableListOf<TieredParcelBaselineAdjuster>(
            PercentTieredParcelBaselineAdjuster(10.8),
            TieredTieredParcelBaselineAdjuster(
                mutableListOf<Double>(6288.0, 7508.0, 7779.0),
                mutableListOf<Double>(0.81, 0.9, 1.15),
            ),
            CapTieredParcelBaselineAdjuster(13600.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 32122.9,
    )
}
