package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area variance calculation. */
interface ServiceAreaVarianceFactor {
    fun describe(): String
    fun adjust(item: ServiceAreaVariance, value: Double): Double
}

data class CapServiceAreaVarianceAdjuster(var cap: Double) : ServiceAreaVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double = minOf(value, cap)
}

data class ServiceAreaVarianceAmountOffsetAdjuster(var weight: Double) : ServiceAreaVarianceFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double = value + item.amount * weight
}

data class RoundingServiceAreaVarianceAdjuster(var step: Double) : ServiceAreaVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double = roundTo(value, step)
}

data class TieredServiceAreaVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentServiceAreaVarianceAdjuster(var percent: Double) : ServiceAreaVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumServiceAreaVarianceAdjuster(var minimum: Double) : ServiceAreaVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area variance steps in order and keeps a trace of each. */
data class ServiceAreaVarianceEngine(
    var steps: MutableList<ServiceAreaVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaVariance): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaVarianceEngine(): ServiceAreaVarianceEngine {
    return ServiceAreaVarianceEngine(
        steps = mutableListOf<ServiceAreaVarianceFactor>(
            CapServiceAreaVarianceAdjuster(21114.0),
            ServiceAreaVarianceAmountOffsetAdjuster(1.38),
            RoundingServiceAreaVarianceAdjuster(0.25),
            TieredServiceAreaVarianceAdjuster(
                mutableListOf<Double>(453.0, 9989.0),
                mutableListOf<Double>(1.19, 0.94),
            ),
            PercentServiceAreaVarianceAdjuster(-4.1),
            MinimumServiceAreaVarianceAdjuster(11.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 93595.9,
    )
}
