package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation variance calculation. */
interface SortationVarianceFactor {
    fun describe(): String
    fun adjust(item: SortationVariance, value: Double): Double
}

data class SortationVarianceCapacityOffsetAdjuster(var weight: Double) : SortationVarianceFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: SortationVariance, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class FlaggedSortationVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSortationVarianceAdjuster(var step: Double) : SortationVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the sortation variance steps in order and keeps a trace of each. */
data class SortationVarianceEngine(
    var steps: MutableList<SortationVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationVariance): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationVarianceEngine(flags: Registry): SortationVarianceEngine {
    return SortationVarianceEngine(
        steps = mutableListOf<SortationVarianceFactor>(
            SortationVarianceCapacityOffsetAdjuster(0.71),
            FlaggedSortationVarianceAdjuster(flags, "sortation-variance-primary", 9.1),
            RoundingSortationVarianceAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 99139.4,
    )
}
