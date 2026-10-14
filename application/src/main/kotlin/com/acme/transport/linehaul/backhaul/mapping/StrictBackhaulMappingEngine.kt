package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict backhaul mapping calculation. */
interface StrictBackhaulMappingStep {
    fun describe(): String
    fun adjust(item: StrictBackhaulMapping, value: Double): Double
}

data class LaneStrictBackhaulMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictBackhaulMappingStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: StrictBackhaulMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictBackhaulMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictBackhaulMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictBackhaulMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictBackhaulMappingAdjuster(var minimum: Double) : StrictBackhaulMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictBackhaulMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict backhaul mapping steps in order and keeps a trace of each. */
data class StrictBackhaulMappingEngine(
    var steps: MutableList<StrictBackhaulMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictBackhaulMapping): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictBackhaulMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictBackhaulMappingEngine(): StrictBackhaulMappingEngine {
    return StrictBackhaulMappingEngine(
        steps = mutableListOf<StrictBackhaulMappingStep>(
            LaneStrictBackhaulMappingAdjuster(mutableMapOf<String, Double>("west" to 1.38, "east" to 1.15)),
            TieredStrictBackhaulMappingAdjuster(
                mutableListOf<Double>(3302.0, 5553.0, 9101.0),
                mutableListOf<Double>(1.11, 0.82, 1.11),
            ),
            MinimumStrictBackhaulMappingAdjuster(9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 45345.0,
    )
}
