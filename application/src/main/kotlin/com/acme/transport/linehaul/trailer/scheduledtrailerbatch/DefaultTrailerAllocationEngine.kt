package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default trailer allocation calculation. */
interface DefaultTrailerAllocationAdjuster {
    fun describe(): String
    fun adjust(item: DefaultTrailerAllocation, value: Double): Double
}

data class TieredDefaultTrailerAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTrailerAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTrailerAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultTrailerAllocationScoreOffsetAdjuster(var weight: Double) : DefaultTrailerAllocationAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: DefaultTrailerAllocation, value: Double): Double = value + item.score * weight
}

data class CapDefaultTrailerAllocationAdjuster(var cap: Double) : DefaultTrailerAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the default trailer allocation steps in order and keeps a trace of each. */
data class DefaultTrailerAllocationEngine(
    var steps: MutableList<DefaultTrailerAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultTrailerAllocation): Double {
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

    fun evaluateAll(items: MutableList<DefaultTrailerAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultTrailerAllocationEngine(): DefaultTrailerAllocationEngine {
    return DefaultTrailerAllocationEngine(
        steps = mutableListOf<DefaultTrailerAllocationAdjuster>(
            TieredDefaultTrailerAllocationAdjuster(
                mutableListOf<Double>(2437.0, 8888.0),
                mutableListOf<Double>(0.83, 0.93),
            ),
            DefaultTrailerAllocationScoreOffsetAdjuster(1.57),
            CapDefaultTrailerAllocationAdjuster(19268.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 11268.2,
    )
}
