package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the service area checkpoint calculation. */
interface ServiceAreaCheckpointModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaCheckpoint, value: Double): Double
}

data class RoundingServiceAreaCheckpointAdjuster(var step: Double) : ServiceAreaCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ServiceAreaCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentServiceAreaCheckpointAdjuster(var percent: Double) : ServiceAreaCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ServiceAreaCheckpointRatioOffsetAdjuster(var weight: Double) : ServiceAreaCheckpointModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ServiceAreaCheckpoint, value: Double): Double = value + item.ratio * weight
}

data class MinimumServiceAreaCheckpointAdjuster(var minimum: Double) : ServiceAreaCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the service area checkpoint steps in order and keeps a trace of each. */
data class ServiceAreaCheckpointEngine(
    var steps: MutableList<ServiceAreaCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServiceAreaCheckpoint): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaCheckpointModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServiceAreaCheckpointEngine(): ServiceAreaCheckpointEngine {
    return ServiceAreaCheckpointEngine(
        steps = mutableListOf<ServiceAreaCheckpointModifier>(
            RoundingServiceAreaCheckpointAdjuster(0.5),
            PercentServiceAreaCheckpointAdjuster(15.6),
            ServiceAreaCheckpointRatioOffsetAdjuster(0.28),
            MinimumServiceAreaCheckpointAdjuster(15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 30469.3,
    )
}
