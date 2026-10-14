package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection mapping calculation. */
interface InspectionMappingStep {
    fun describe(): String
    fun adjust(item: InspectionMapping, value: Double): Double
}

data class InspectionMappingDiscountOffsetAdjuster(var weight: Double) : InspectionMappingStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: InspectionMapping, value: Double): Double = value + item.discount * weight
}

data class TieredInspectionMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionMappingAdjuster(var minimum: Double) : InspectionMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedInspectionMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionMappingAdjuster(var cap: Double) : InspectionMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionMapping, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionMappingAdjuster(var step: Double) : InspectionMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionMapping, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection mapping steps in order and keeps a trace of each. */
data class InspectionMappingEngine(
    var steps: MutableList<InspectionMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionMapping): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionMappingEngine(flags: Registry): InspectionMappingEngine {
    return InspectionMappingEngine(
        steps = mutableListOf<InspectionMappingStep>(
            InspectionMappingDiscountOffsetAdjuster(1.92),
            TieredInspectionMappingAdjuster(
                mutableListOf<Double>(2645.0, 4373.0, 8435.0, 9386.0),
                mutableListOf<Double>(1.0, 1.11, 1.05, 0.87),
            ),
            MinimumInspectionMappingAdjuster(37.0),
            FlaggedInspectionMappingAdjuster(flags, "inspection-mapping-legacy", -2.3),
            CapInspectionMappingAdjuster(9226.0),
            RoundingInspectionMappingAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 29040.3,
    )
}
