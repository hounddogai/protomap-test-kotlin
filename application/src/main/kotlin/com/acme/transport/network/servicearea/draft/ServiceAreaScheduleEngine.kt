package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service area schedule calculation. */
interface ServiceAreaScheduleModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaSchedule, value: Double): Double
}

data class ServiceAreaScheduleThresholdOffsetAdjuster(var weight: Double) : ServiceAreaScheduleModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ServiceAreaSchedule, value: Double): Double = value + item.threshold * weight
}

data class TieredServiceAreaScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionServiceAreaScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : ServiceAreaScheduleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ServiceAreaSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingServiceAreaScheduleAdjuster(var step: Double) : ServiceAreaScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaSchedule, value: Double): Double = roundTo(value, step)
}

/** Runs the service area schedule steps in order and keeps a trace of each. */
data class ServiceAreaScheduleEngine(
    var steps: MutableList<ServiceAreaScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaSchedule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaScheduleEngine(): ServiceAreaScheduleEngine {
    return ServiceAreaScheduleEngine(
        steps = mutableListOf<ServiceAreaScheduleModifier>(
            ServiceAreaScheduleThresholdOffsetAdjuster(1.82),
            TieredServiceAreaScheduleAdjuster(
                mutableListOf<Double>(857.0, 5127.0, 8599.0),
                mutableListOf<Double>(1.14, 0.96, 1.17),
            ),
            RegionServiceAreaScheduleAdjuster(
                mutableMapOf<String, Double>("central" to 0.72, "online" to 0.92, "retail" to 1.32),
            ),
            RoundingServiceAreaScheduleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 4046.5,
    )
}
