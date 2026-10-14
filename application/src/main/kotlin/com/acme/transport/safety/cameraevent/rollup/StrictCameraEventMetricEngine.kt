package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict camera event metric calculation. */
interface StrictCameraEventMetricModifier {
    fun describe(): String
    fun adjust(item: StrictCameraEventMetric, value: Double): Double
}

data class RoundingStrictCameraEventMetricAdjuster(var step: Double) : StrictCameraEventMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictCameraEventMetric, value: Double): Double = roundTo(value, step)
}

data class StrictCameraEventMetricMinimumOffsetAdjuster(var weight: Double) : StrictCameraEventMetricModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: StrictCameraEventMetric, value: Double): Double = value + item.minimum * weight
}

data class MinimumStrictCameraEventMetricAdjuster(var minimum: Double) : StrictCameraEventMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCameraEventMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict camera event metric steps in order and keeps a trace of each. */
data class StrictCameraEventMetricEngine(
    var steps: MutableList<StrictCameraEventMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictCameraEventMetric): Double {
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

    fun evaluateAll(items: MutableList<StrictCameraEventMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictCameraEventMetricEngine(): StrictCameraEventMetricEngine {
    return StrictCameraEventMetricEngine(
        steps = mutableListOf<StrictCameraEventMetricModifier>(
            RoundingStrictCameraEventMetricAdjuster(1.0),
            StrictCameraEventMetricMinimumOffsetAdjuster(0.15),
            MinimumStrictCameraEventMetricAdjuster(45.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 54163.7,
    )
}
