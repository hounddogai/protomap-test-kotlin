package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the service plan checkpoint calculation. */
interface ServicePlanCheckpointModifier {
    fun describe(): String
    fun adjust(item: ServicePlanCheckpoint, value: Double): Double
}

data class RoundingServicePlanCheckpointAdjuster(var step: Double) : ServicePlanCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServicePlanCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredServicePlanCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanCheckpointModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentServicePlanCheckpointAdjuster(var percent: Double) : ServicePlanCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ServicePlanCheckpointScoreOffsetAdjuster(var weight: Double) : ServicePlanCheckpointModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ServicePlanCheckpoint, value: Double): Double = value + item.score * weight
}

/** Runs the service plan checkpoint steps in order and keeps a trace of each. */
data class ServicePlanCheckpointEngine(
    var steps: MutableList<ServicePlanCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ServicePlanCheckpoint): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServicePlanCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultServicePlanCheckpointEngine(): ServicePlanCheckpointEngine {
    return ServicePlanCheckpointEngine(
        steps = mutableListOf<ServicePlanCheckpointModifier>(
            RoundingServicePlanCheckpointAdjuster(5.0),
            TieredServicePlanCheckpointAdjuster(
                mutableListOf<Double>(5518.0, 8848.0, 9225.0),
                mutableListOf<Double>(1.17, 0.89, 1.16),
            ),
            PercentServicePlanCheckpointAdjuster(1.4),
            ServicePlanCheckpointScoreOffsetAdjuster(1.89),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 68136.1,
    )
}
