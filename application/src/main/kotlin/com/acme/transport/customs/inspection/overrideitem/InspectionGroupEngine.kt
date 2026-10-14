package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection group calculation. */
interface InspectionGroupAdjuster {
    fun describe(): String
    fun adjust(item: InspectionGroup, value: Double): Double
}

data class TieredInspectionGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionGroupAdjuster(var minimum: Double) : InspectionGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingInspectionGroupAdjuster(var step: Double) : InspectionGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionGroup, value: Double): Double = roundTo(value, step)
}

data class CapInspectionGroupAdjuster(var cap: Double) : InspectionGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection group steps in order and keeps a trace of each. */
data class InspectionGroupEngine(
    var steps: MutableList<InspectionGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionGroup): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionGroupEngine(): InspectionGroupEngine {
    return InspectionGroupEngine(
        steps = mutableListOf<InspectionGroupAdjuster>(
            TieredInspectionGroupAdjuster(
                mutableListOf<Double>(1925.0, 6819.0),
                mutableListOf<Double>(1.12, 1.07),
            ),
            MinimumInspectionGroupAdjuster(43.7),
            RoundingInspectionGroupAdjuster(0.5),
            CapInspectionGroupAdjuster(1248.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 60778.9,
    )
}
