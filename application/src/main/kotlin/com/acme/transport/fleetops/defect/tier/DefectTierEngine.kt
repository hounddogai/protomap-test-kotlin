package com.acme.transport.fleetops.defect.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect tier calculation. */
interface DefectTierAdjuster {
    fun describe(): String
    fun adjust(item: DefectTier, value: Double): Double
}

data class TieredDefectTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefectTierAdjuster(var minimum: Double) : DefectTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefectTierAdjuster(var cap: Double) : DefectTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectTier, value: Double): Double = minOf(value, cap)
}

/** Runs the defect tier steps in order and keeps a trace of each. */
data class DefectTierEngine(
    var steps: MutableList<DefectTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefectTier): Double {
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

    fun evaluateAll(items: MutableList<DefectTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefectTierEngine(): DefectTierEngine {
    return DefectTierEngine(
        steps = mutableListOf<DefectTierAdjuster>(
            TieredDefectTierAdjuster(
                mutableListOf<Double>(2496.0, 5502.0, 5713.0),
                mutableListOf<Double>(0.82, 1.11, 0.81),
            ),
            MinimumDefectTierAdjuster(14.0),
            CapDefectTierAdjuster(30546.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 34650.6,
    )
}
