package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional rest break adjustment calculation. */
interface RegionalRestBreakAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: RegionalRestBreakAdjustment, value: Double): Double
}

data class CapRegionalRestBreakAdjustmentAdjuster(var cap: Double) : RegionalRestBreakAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRestBreakAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalRestBreakAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRestBreakAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRestBreakAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameRegionalRestBreakAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalRestBreakAdjustmentAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalRestBreakAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalRestBreakAdjustmentEtaMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalRestBreakAdjustmentAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: RegionalRestBreakAdjustment, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

/** Runs the regional rest break adjustment steps in order and keeps a trace of each. */
data class RegionalRestBreakAdjustmentEngine(
    var steps: MutableList<RegionalRestBreakAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRestBreakAdjustment): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalRestBreakAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRestBreakAdjustmentEngine(): RegionalRestBreakAdjustmentEngine {
    return RegionalRestBreakAdjustmentEngine(
        steps = mutableListOf<RegionalRestBreakAdjustmentAdjuster>(
            CapRegionalRestBreakAdjustmentAdjuster(43274.0),
            TieredRegionalRestBreakAdjustmentAdjuster(
                mutableListOf<Double>(4285.0, 5983.0, 6346.0),
                mutableListOf<Double>(0.82, 0.96, 0.81),
            ),
            NameRegionalRestBreakAdjustmentAdjuster(mutableMapOf<String, Double>("online" to 1.09)),
            RegionalRestBreakAdjustmentEtaMinutesOffsetAdjuster(0.28),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 12837.7,
    )
}
