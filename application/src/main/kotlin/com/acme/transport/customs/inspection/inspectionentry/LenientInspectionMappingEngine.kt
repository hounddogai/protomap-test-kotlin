package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient inspection mapping calculation. */
interface LenientInspectionMappingStep {
    fun describe(): String
    fun adjust(item: LenientInspectionMapping, value: Double): Double
}

data class PercentLenientInspectionMappingAdjuster(var percent: Double) : LenientInspectionMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientInspectionMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientInspectionMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientInspectionMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientInspectionMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientInspectionMappingAdjuster(var minimum: Double) : LenientInspectionMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientInspectionMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientInspectionMappingAdjuster(var cap: Double) : LenientInspectionMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientInspectionMapping, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient inspection mapping steps in order and keeps a trace of each. */
data class LenientInspectionMappingEngine(
    var steps: MutableList<LenientInspectionMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientInspectionMapping): Double {
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

    fun evaluateAll(items: MutableList<LenientInspectionMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientInspectionMappingEngine(): LenientInspectionMappingEngine {
    return LenientInspectionMappingEngine(
        steps = mutableListOf<LenientInspectionMappingStep>(
            PercentLenientInspectionMappingAdjuster(-6.7),
            TieredLenientInspectionMappingAdjuster(
                mutableListOf<Double>(5169.0, 5565.0, 7371.0, 8816.0),
                mutableListOf<Double>(0.87, 1.19, 1.07, 0.85),
            ),
            MinimumLenientInspectionMappingAdjuster(20.8),
            CapLenientInspectionMappingAdjuster(13030.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 76157.0,
    )
}
