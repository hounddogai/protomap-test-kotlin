package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global swap metric calculation. */
interface GlobalSwapMetricAdjuster {
    fun describe(): String
    fun adjust(item: GlobalSwapMetric, value: Double): Double
}

data class RoundingGlobalSwapMetricAdjuster(var step: Double) : GlobalSwapMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalSwapMetric, value: Double): Double = roundTo(value, step)
}

data class CapGlobalSwapMetricAdjuster(var cap: Double) : GlobalSwapMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalSwapMetric, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalSwapMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalSwapMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalSwapMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalSwapMetricAdjuster(var minimum: Double) : GlobalSwapMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalSwapMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneGlobalSwapMetricAdjuster(var factors: MutableMap<String, Double>) : GlobalSwapMetricAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: GlobalSwapMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global swap metric steps in order and keeps a trace of each. */
data class GlobalSwapMetricEngine(
    var steps: MutableList<GlobalSwapMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalSwapMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalSwapMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalSwapMetricEngine(): GlobalSwapMetricEngine {
    return GlobalSwapMetricEngine(
        steps = mutableListOf<GlobalSwapMetricAdjuster>(
            RoundingGlobalSwapMetricAdjuster(0.1),
            CapGlobalSwapMetricAdjuster(40971.0),
            TieredGlobalSwapMetricAdjuster(
                mutableListOf<Double>(5455.0, 6853.0, 7928.0, 8145.0),
                mutableListOf<Double>(1.11, 1.15, 1.2, 1.15),
            ),
            MinimumGlobalSwapMetricAdjuster(38.4),
            LaneGlobalSwapMetricAdjuster(
                mutableMapOf<String, Double>("central" to 1.17, "online" to 1.19, "retail" to 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 17001.6,
    )
}
