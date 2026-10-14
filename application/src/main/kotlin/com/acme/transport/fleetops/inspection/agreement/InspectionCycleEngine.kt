package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection cycle calculation. */
interface InspectionCycleFactor {
    fun describe(): String
    fun adjust(item: InspectionCycle, value: Double): Double
}

data class TieredInspectionCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingInspectionCycleAdjuster(var step: Double) : InspectionCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionCycle, value: Double): Double = roundTo(value, step)
}

data class MinimumInspectionCycleAdjuster(var minimum: Double) : InspectionCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class InspectionCycleQuantityOffsetAdjuster(var weight: Double) : InspectionCycleFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: InspectionCycle, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the inspection cycle steps in order and keeps a trace of each. */
data class InspectionCycleEngine(
    var steps: MutableList<InspectionCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionCycle): Double {
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

    fun evaluateAll(items: MutableList<InspectionCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionCycleEngine(): InspectionCycleEngine {
    return InspectionCycleEngine(
        steps = mutableListOf<InspectionCycleFactor>(
            TieredInspectionCycleAdjuster(
                mutableListOf<Double>(2728.0, 3236.0, 9459.0),
                mutableListOf<Double>(1.13, 0.99, 0.83),
            ),
            RoundingInspectionCycleAdjuster(0.05),
            MinimumInspectionCycleAdjuster(46.1),
            InspectionCycleQuantityOffsetAdjuster(0.48),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 74336.4,
    )
}
