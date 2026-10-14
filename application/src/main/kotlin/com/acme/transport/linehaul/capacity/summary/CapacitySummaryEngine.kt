package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity summary calculation. */
interface CapacitySummaryFactor {
    fun describe(): String
    fun adjust(item: CapacitySummary, value: Double): Double
}

data class RoundingCapacitySummaryAdjuster(var step: Double) : CapacitySummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacitySummary, value: Double): Double = roundTo(value, step)
}

data class TieredCapacitySummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacitySummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacitySummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCapacitySummaryAdjuster(var percent: Double) : CapacitySummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacitySummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCapacitySummaryAdjuster(var cap: Double) : CapacitySummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacitySummary, value: Double): Double = minOf(value, cap)
}

data class CapacitySummaryUpdatedAtOffsetAdjuster(var weight: Double) : CapacitySummaryFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: CapacitySummary, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class MinimumCapacitySummaryAdjuster(var minimum: Double) : CapacitySummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacitySummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capacity summary steps in order and keeps a trace of each. */
data class CapacitySummaryEngine(
    var steps: MutableList<CapacitySummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacitySummary): Double {
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

    fun evaluateAll(items: MutableList<CapacitySummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacitySummaryEngine(): CapacitySummaryEngine {
    return CapacitySummaryEngine(
        steps = mutableListOf<CapacitySummaryFactor>(
            RoundingCapacitySummaryAdjuster(5.0),
            TieredCapacitySummaryAdjuster(
                mutableListOf<Double>(3865.0, 6039.0),
                mutableListOf<Double>(0.92, 1.0),
            ),
            PercentCapacitySummaryAdjuster(-1.5),
            CapCapacitySummaryAdjuster(45672.0),
            CapacitySummaryUpdatedAtOffsetAdjuster(0.42),
            MinimumCapacitySummaryAdjuster(16.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51736.0,
    )
}
