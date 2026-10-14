package com.acme.transport.crewing.swap.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap mapping calculation. */
interface SwapMappingStep {
    fun describe(): String
    fun adjust(item: SwapMapping, value: Double): Double
}

data class CapSwapMappingAdjuster(var cap: Double) : SwapMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapMapping, value: Double): Double = minOf(value, cap)
}

data class PercentSwapMappingAdjuster(var percent: Double) : SwapMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SwapMappingExpiresAtOffsetAdjuster(var weight: Double) : SwapMappingStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: SwapMapping, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class LabelSwapMappingAdjuster(var factors: MutableMap<String, Double>) : SwapMappingStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SwapMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSwapMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap mapping steps in order and keeps a trace of each. */
data class SwapMappingEngine(
    var steps: MutableList<SwapMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapMapping): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapMappingEngine(): SwapMappingEngine {
    return SwapMappingEngine(
        steps = mutableListOf<SwapMappingStep>(
            CapSwapMappingAdjuster(22470.0),
            PercentSwapMappingAdjuster(4.8),
            SwapMappingExpiresAtOffsetAdjuster(0.64),
            LabelSwapMappingAdjuster(
                mutableMapOf<String, Double>("east" to 1.08, "central" to 1.12, "retail" to 1.05),
            ),
            TieredSwapMappingAdjuster(
                mutableListOf<Double>(4803.0, 5292.0, 6609.0, 7356.0),
                mutableListOf<Double>(1.03, 1.1, 1.0, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 74324.7,
    )
}
