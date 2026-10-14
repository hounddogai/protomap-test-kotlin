package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive route offset manifest calculation. */
interface AdaptiveRouteOffsetManifestFactor {
    fun describe(): String
    fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double
}

data class RoundingAdaptiveRouteOffsetManifestAdjuster(var step: Double) : AdaptiveRouteOffsetManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveRouteOffsetManifestAdjuster(var cap: Double) : AdaptiveRouteOffsetManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveRouteOffsetManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveRouteOffsetManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveRouteOffsetManifestQuantityOffsetAdjuster(
    var weight: Double,
) : AdaptiveRouteOffsetManifestFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class UnitAdaptiveRouteOffsetManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveRouteOffsetManifestFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: AdaptiveRouteOffsetManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive route offset manifest steps in order and keeps a trace of each. */
data class AdaptiveRouteOffsetManifestEngine(
    var steps: MutableList<AdaptiveRouteOffsetManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveRouteOffsetManifest): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveRouteOffsetManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveRouteOffsetManifestEngine(): AdaptiveRouteOffsetManifestEngine {
    return AdaptiveRouteOffsetManifestEngine(
        steps = mutableListOf<AdaptiveRouteOffsetManifestFactor>(
            RoundingAdaptiveRouteOffsetManifestAdjuster(0.1),
            CapAdaptiveRouteOffsetManifestAdjuster(29996.0),
            TieredAdaptiveRouteOffsetManifestAdjuster(
                mutableListOf<Double>(3763.0, 4107.0, 8794.0, 8842.0),
                mutableListOf<Double>(1.03, 0.92, 0.95, 1.04),
            ),
            AdaptiveRouteOffsetManifestQuantityOffsetAdjuster(1.94),
            UnitAdaptiveRouteOffsetManifestAdjuster(
                mutableMapOf<String, Double>("west" to 1.4, "east" to 1.1, "online" to 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 33625.6,
    )
}
