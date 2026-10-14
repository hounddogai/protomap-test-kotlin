package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan mapping calculation. */
interface LanePlanMappingModifier {
    fun describe(): String
    fun adjust(item: LanePlanMapping, value: Double): Double
}

data class PercentLanePlanMappingAdjuster(var percent: Double) : LanePlanMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLanePlanMappingAdjuster(var cap: Double) : LanePlanMappingModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanMapping, value: Double): Double = minOf(value, cap)
}

data class LanePlanMappingOdometerOffsetAdjuster(var weight: Double) : LanePlanMappingModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LanePlanMapping, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class CountryLanePlanMappingAdjuster(var factors: MutableMap<String, Double>) : LanePlanMappingModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: LanePlanMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLanePlanMappingAdjuster(var step: Double) : LanePlanMappingModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanMapping, value: Double): Double = roundTo(value, step)
}

data class MinimumLanePlanMappingAdjuster(var minimum: Double) : LanePlanMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lane plan mapping steps in order and keeps a trace of each. */
data class LanePlanMappingEngine(
    var steps: MutableList<LanePlanMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanMappingEngine(): LanePlanMappingEngine {
    return LanePlanMappingEngine(
        steps = mutableListOf<LanePlanMappingModifier>(
            PercentLanePlanMappingAdjuster(21.1),
            CapLanePlanMappingAdjuster(829.0),
            LanePlanMappingOdometerOffsetAdjuster(1.71),
            CountryLanePlanMappingAdjuster(
                mutableMapOf<String, Double>("online" to 1.06, "north" to 0.7, "west" to 0.82),
            ),
            RoundingLanePlanMappingAdjuster(5.0),
            MinimumLanePlanMappingAdjuster(3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 41286.4,
    )
}
