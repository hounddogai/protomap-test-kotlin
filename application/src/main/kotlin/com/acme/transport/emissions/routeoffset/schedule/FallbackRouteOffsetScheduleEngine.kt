package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback route offset schedule calculation. */
interface FallbackRouteOffsetScheduleModifier {
    fun describe(): String
    fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double
}

data class MinimumFallbackRouteOffsetScheduleAdjuster(
    var minimum: Double,
) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackRouteOffsetScheduleDiscountOffsetAdjuster(
    var weight: Double,
) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double = value + item.discount * weight
}

data class CapFallbackRouteOffsetScheduleAdjuster(var cap: Double) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackRouteOffsetScheduleAdjuster(var step: Double) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double = roundTo(value, step)
}

data class NameFallbackRouteOffsetScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFallbackRouteOffsetScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRouteOffsetScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRouteOffsetSchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback route offset schedule steps in order and keeps a trace of each. */
data class FallbackRouteOffsetScheduleEngine(
    var steps: MutableList<FallbackRouteOffsetScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackRouteOffsetSchedule): Double {
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

    fun evaluateAll(items: MutableList<FallbackRouteOffsetSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackRouteOffsetScheduleEngine(): FallbackRouteOffsetScheduleEngine {
    return FallbackRouteOffsetScheduleEngine(
        steps = mutableListOf<FallbackRouteOffsetScheduleModifier>(
            MinimumFallbackRouteOffsetScheduleAdjuster(15.4),
            FallbackRouteOffsetScheduleDiscountOffsetAdjuster(1.31),
            CapFallbackRouteOffsetScheduleAdjuster(49055.0),
            RoundingFallbackRouteOffsetScheduleAdjuster(0.1),
            NameFallbackRouteOffsetScheduleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.29, "central" to 0.9),
            ),
            TieredFallbackRouteOffsetScheduleAdjuster(
                mutableListOf<Double>(5316.0, 5752.0),
                mutableListOf<Double>(0.82, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 10343.8,
    )
}
