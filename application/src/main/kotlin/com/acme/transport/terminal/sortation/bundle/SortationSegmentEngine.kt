package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation segment calculation. */
interface SortationSegmentStep {
    fun describe(): String
    fun adjust(item: SortationSegment, value: Double): Double
}

data class TieredSortationSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSortationSegmentAdjuster(var step: Double) : SortationSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationSegment, value: Double): Double = roundTo(value, step)
}

data class PercentSortationSegmentAdjuster(var percent: Double) : SortationSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the sortation segment steps in order and keeps a trace of each. */
data class SortationSegmentEngine(
    var steps: MutableList<SortationSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationSegment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationSegmentEngine(): SortationSegmentEngine {
    return SortationSegmentEngine(
        steps = mutableListOf<SortationSegmentStep>(
            TieredSortationSegmentAdjuster(
                mutableListOf<Double>(4961.0, 6539.0),
                mutableListOf<Double>(0.83, 0.81),
            ),
            RoundingSortationSegmentAdjuster(0.05),
            PercentSortationSegmentAdjuster(9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 73514.2,
    )
}
