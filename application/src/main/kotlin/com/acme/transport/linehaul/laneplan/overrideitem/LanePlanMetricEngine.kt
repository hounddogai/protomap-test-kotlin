package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan metric calculation. */
interface LanePlanMetricModifier {
    fun describe(): String
    fun adjust(item: LanePlanMetric, value: Double): Double
}

data class RoundingLanePlanMetricAdjuster(var step: Double) : LanePlanMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanMetric, value: Double): Double = roundTo(value, step)
}

data class TieredLanePlanMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LanePlanMetricBudgetOffsetAdjuster(var weight: Double) : LanePlanMetricModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: LanePlanMetric, value: Double): Double = value + item.budget * weight
}

/** Runs the lane plan metric steps in order and keeps a trace of each. */
data class LanePlanMetricEngine(
    var steps: MutableList<LanePlanMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanMetric): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanMetricEngine(): LanePlanMetricEngine {
    return LanePlanMetricEngine(
        steps = mutableListOf<LanePlanMetricModifier>(
            RoundingLanePlanMetricAdjuster(0.5),
            TieredLanePlanMetricAdjuster(
                mutableListOf<Double>(2572.0, 3789.0, 9651.0),
                mutableListOf<Double>(0.83, 0.97, 0.96),
            ),
            LanePlanMetricBudgetOffsetAdjuster(1.68),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 75249.2,
    )
}
