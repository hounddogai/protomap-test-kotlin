package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global cross dock summary calculation. */
interface GlobalCrossDockSummaryFactor {
    fun describe(): String
    fun adjust(item: GlobalCrossDockSummary, value: Double): Double
}

data class TieredGlobalCrossDockSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCrossDockSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCrossDockSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalCrossDockSummaryAdjuster(var percent: Double) : GlobalCrossDockSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCrossDockSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalCrossDockSummaryQuantityOffsetAdjuster(var weight: Double) : GlobalCrossDockSummaryFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: GlobalCrossDockSummary, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class CapGlobalCrossDockSummaryAdjuster(var cap: Double) : GlobalCrossDockSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCrossDockSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the global cross dock summary steps in order and keeps a trace of each. */
data class GlobalCrossDockSummaryEngine(
    var steps: MutableList<GlobalCrossDockSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalCrossDockSummary): Double {
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

    fun evaluateAll(items: MutableList<GlobalCrossDockSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalCrossDockSummaryEngine(): GlobalCrossDockSummaryEngine {
    return GlobalCrossDockSummaryEngine(
        steps = mutableListOf<GlobalCrossDockSummaryFactor>(
            TieredGlobalCrossDockSummaryAdjuster(
                mutableListOf<Double>(1506.0, 6659.0, 6712.0),
                mutableListOf<Double>(1.03, 0.97, 1.19),
            ),
            PercentGlobalCrossDockSummaryAdjuster(-23.9),
            GlobalCrossDockSummaryQuantityOffsetAdjuster(0.89),
            CapGlobalCrossDockSummaryAdjuster(22341.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 3114.9,
    )
}
