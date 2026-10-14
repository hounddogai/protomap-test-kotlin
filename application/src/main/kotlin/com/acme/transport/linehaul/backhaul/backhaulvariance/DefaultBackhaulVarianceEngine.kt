package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default backhaul variance calculation. */
interface DefaultBackhaulVarianceFactor {
    fun describe(): String
    fun adjust(item: DefaultBackhaulVariance, value: Double): Double
}

data class CapDefaultBackhaulVarianceAdjuster(var cap: Double) : DefaultBackhaulVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultBackhaulVariance, value: Double): Double = minOf(value, cap)
}

data class RegionDefaultBackhaulVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultBackhaulVarianceFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultBackhaulVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultBackhaulVarianceThresholdOffsetAdjuster(var weight: Double) : DefaultBackhaulVarianceFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: DefaultBackhaulVariance, value: Double): Double = value + item.threshold * weight
}

data class TieredDefaultBackhaulVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultBackhaulVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultBackhaulVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default backhaul variance steps in order and keeps a trace of each. */
data class DefaultBackhaulVarianceEngine(
    var steps: MutableList<DefaultBackhaulVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultBackhaulVariance): Double {
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

    fun evaluateAll(items: MutableList<DefaultBackhaulVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultBackhaulVarianceEngine(): DefaultBackhaulVarianceEngine {
    return DefaultBackhaulVarianceEngine(
        steps = mutableListOf<DefaultBackhaulVarianceFactor>(
            CapDefaultBackhaulVarianceAdjuster(31300.0),
            RegionDefaultBackhaulVarianceAdjuster(mutableMapOf<String, Double>("retail" to 0.9)),
            DefaultBackhaulVarianceThresholdOffsetAdjuster(1.9),
            TieredDefaultBackhaulVarianceAdjuster(
                mutableListOf<Double>(3304.0, 7284.0),
                mutableListOf<Double>(1.02, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 98159.1,
    )
}
