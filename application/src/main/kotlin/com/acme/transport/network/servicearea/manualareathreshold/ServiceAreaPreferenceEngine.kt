package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area preference calculation. */
interface ServiceAreaPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaPreference, value: Double): Double
}

data class TieredServiceAreaPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryServiceAreaPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaPreferenceAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ServiceAreaPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ServiceAreaPreferenceEndsAtOffsetAdjuster(var weight: Double) : ServiceAreaPreferenceAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: ServiceAreaPreference, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the service area preference steps in order and keeps a trace of each. */
data class ServiceAreaPreferenceEngine(
    var steps: MutableList<ServiceAreaPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaPreference): Double {
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

    fun evaluateAll(items: MutableList<ServiceAreaPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaPreferenceEngine(): ServiceAreaPreferenceEngine {
    return ServiceAreaPreferenceEngine(
        steps = mutableListOf<ServiceAreaPreferenceAdjuster>(
            TieredServiceAreaPreferenceAdjuster(
                mutableListOf<Double>(2352.0, 4828.0),
                mutableListOf<Double>(0.88, 0.81),
            ),
            CategoryServiceAreaPreferenceAdjuster(mutableMapOf<String, Double>("retail" to 1.1)),
            ServiceAreaPreferenceEndsAtOffsetAdjuster(0.21),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 4259.8,
    )
}
