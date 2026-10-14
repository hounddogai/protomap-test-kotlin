package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback route offset reservation calculation. */
interface FallbackRouteOffsetReservationStep {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetReservation, value: Double): Double
}

data class TitleFallbackRouteOffsetReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRouteOffsetReservationStep {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackRouteOffsetReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackRouteOffsetReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRouteOffsetReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRouteOffsetReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackRouteOffsetReservationAdjuster(
    var percent: Double,
) : FallbackRouteOffsetReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRouteOffsetReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackRouteOffsetReservationAdjuster(
    var step: Double,
) : FallbackRouteOffsetReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRouteOffsetReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback route offset reservation steps in order and keeps a trace of each. */
data class FallbackRouteOffsetReservationEngine(
    var steps: MutableList<FallbackRouteOffsetReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackRouteOffsetReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRouteOffsetReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetReservationEngine(): FallbackRouteOffsetReservationEngine {
    return FallbackRouteOffsetReservationEngine(
        steps = mutableListOf<FallbackRouteOffsetReservationStep>(
            TitleFallbackRouteOffsetReservationAdjuster(
                mutableMapOf<String, Double>("west" to 1.03, "south" to 1.15),
            ),
            TieredFallbackRouteOffsetReservationAdjuster(
                mutableListOf<Double>(1927.0, 8189.0),
                mutableListOf<Double>(0.95, 1.01),
            ),
            PercentFallbackRouteOffsetReservationAdjuster(-21.0),
            RoundingFallbackRouteOffsetReservationAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 48907.1,
    )
}
