package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global backhaul metric calculation. */
interface GlobalBackhaulMetricAdjuster {
    fun describe(): String
    fun adjust(item: GlobalBackhaulMetric, value: Double): Double
}

data class GlobalBackhaulMetricCapacityOffsetAdjuster(var weight: Double) : GlobalBackhaulMetricAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: GlobalBackhaulMetric, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredGlobalBackhaulMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalBackhaulMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalBackhaulMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalBackhaulMetricAdjuster(var percent: Double) : GlobalBackhaulMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalBackhaulMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalBackhaulMetricAdjuster(var minimum: Double) : GlobalBackhaulMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalBackhaulMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalBackhaulMetricAdjuster(var cap: Double) : GlobalBackhaulMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalBackhaulMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the global backhaul metric steps in order and keeps a trace of each. */
data class GlobalBackhaulMetricEngine(
    var steps: MutableList<GlobalBackhaulMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalBackhaulMetric): Double {
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

    fun evaluateAll(items: MutableList<GlobalBackhaulMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalBackhaulMetricEngine(): GlobalBackhaulMetricEngine {
    return GlobalBackhaulMetricEngine(
        steps = mutableListOf<GlobalBackhaulMetricAdjuster>(
            GlobalBackhaulMetricCapacityOffsetAdjuster(0.12),
            TieredGlobalBackhaulMetricAdjuster(
                mutableListOf<Double>(82.0, 4732.0, 9657.0),
                mutableListOf<Double>(1.07, 1.07, 1.02),
            ),
            PercentGlobalBackhaulMetricAdjuster(14.2),
            MinimumGlobalBackhaulMetricAdjuster(10.5),
            CapGlobalBackhaulMetricAdjuster(3467.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 54391.2,
    )
}
