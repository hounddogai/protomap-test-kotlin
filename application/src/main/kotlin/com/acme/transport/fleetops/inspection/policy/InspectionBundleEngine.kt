package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection bundle calculation. */
interface InspectionBundleAdjuster {
    fun describe(): String
    fun adjust(item: InspectionBundle, value: Double): Double
}

data class MinimumInspectionBundleAdjuster(var minimum: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapInspectionBundleAdjuster(var cap: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionBundleAdjuster(var step: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionBundle, value: Double): Double = roundTo(value, step)
}

data class TieredInspectionBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class InspectionBundleFuelLitersOffsetAdjuster(var weight: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: InspectionBundle, value: Double): Double = value + item.fuelLiters * weight
}

data class SegmentInspectionBundleAdjuster(var factors: MutableMap<String, Double>) : InspectionBundleAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: InspectionBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection bundle steps in order and keeps a trace of each. */
data class InspectionBundleEngine(
    var steps: MutableList<InspectionBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionBundle): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionBundleEngine(): InspectionBundleEngine {
    return InspectionBundleEngine(
        steps = mutableListOf<InspectionBundleAdjuster>(
            MinimumInspectionBundleAdjuster(42.3),
            CapInspectionBundleAdjuster(10647.0),
            RoundingInspectionBundleAdjuster(0.05),
            TieredInspectionBundleAdjuster(
                mutableListOf<Double>(455.0, 6366.0, 8705.0, 9563.0),
                mutableListOf<Double>(1.16, 1.06, 0.94, 0.98),
            ),
            InspectionBundleFuelLitersOffsetAdjuster(0.09),
            SegmentInspectionBundleAdjuster(mutableMapOf<String, Double>("online" to 1.03, "west" to 1.1)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 8032.9,
    )
}
