package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default transit time checkpoint calculation. */
interface DefaultTransitTimeCheckpointFactor {
    fun describe(): String
    fun adjust(item: DefaultTransitTimeCheckpoint, value: Double): Double
}

data class MinimumDefaultTransitTimeCheckpointAdjuster(
    var minimum: Double,
) : DefaultTransitTimeCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTransitTimeCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleDefaultTransitTimeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTransitTimeCheckpointFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefaultTransitTimeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultTransitTimeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTransitTimeCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTransitTimeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default transit time checkpoint steps in order and keeps a trace of each. */
data class DefaultTransitTimeCheckpointEngine(
    var steps: MutableList<DefaultTransitTimeCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultTransitTimeCheckpoint): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultTransitTimeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultTransitTimeCheckpointEngine(): DefaultTransitTimeCheckpointEngine {
    return DefaultTransitTimeCheckpointEngine(
        steps = mutableListOf<DefaultTransitTimeCheckpointFactor>(
            MinimumDefaultTransitTimeCheckpointAdjuster(30.3),
            LocaleDefaultTransitTimeCheckpointAdjuster(
                mutableMapOf<String, Double>("east" to 0.72, "north" to 1.02, "retail" to 1.17),
            ),
            TieredDefaultTransitTimeCheckpointAdjuster(
                mutableListOf<Double>(2499.0, 3236.0, 6731.0, 9443.0),
                mutableListOf<Double>(0.82, 0.84, 1.15, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 64737.3,
    )
}
