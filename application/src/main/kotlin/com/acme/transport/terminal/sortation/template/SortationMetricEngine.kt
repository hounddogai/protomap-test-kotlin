package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation metric calculation. */
interface SortationMetricModifier {
    fun describe(): String
    fun adjust(item: SortationMetric, value: Double): Double
}

data class UnitSortationMetricAdjuster(var factors: MutableMap<String, Double>) : SortationMetricModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: SortationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSortationMetricAdjuster(var step: Double) : SortationMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumSortationMetricAdjuster(var minimum: Double) : SortationMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SortationMetricOdometerOffsetAdjuster(var weight: Double) : SortationMetricModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: SortationMetric, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the sortation metric steps in order and keeps a trace of each. */
data class SortationMetricEngine(
    var steps: MutableList<SortationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SortationMetric): Double {
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

    fun evaluateAll(items: MutableList<SortationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSortationMetricEngine(): SortationMetricEngine {
    return SortationMetricEngine(
        steps = mutableListOf<SortationMetricModifier>(
            UnitSortationMetricAdjuster(mutableMapOf<String, Double>("retail" to 0.9, "west" to 1.22)),
            RoundingSortationMetricAdjuster(0.1),
            MinimumSortationMetricAdjuster(28.2),
            SortationMetricOdometerOffsetAdjuster(1.63),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 2065.9,
    )
}
