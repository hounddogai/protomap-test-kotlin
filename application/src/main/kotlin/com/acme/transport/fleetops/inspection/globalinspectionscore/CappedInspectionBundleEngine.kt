package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped inspection bundle calculation. */
interface CappedInspectionBundleModifier {
    fun describe(): String
    fun adjust(item: CappedInspectionBundle, value: Double): Double
}

data class TieredCappedInspectionBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedInspectionBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedInspectionBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedInspectionBundleAdjuster(var minimum: Double) : CappedInspectionBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedInspectionBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedInspectionBundleAdjuster(var step: Double) : CappedInspectionBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedInspectionBundle, value: Double): Double = roundTo(value, step)
}

data class PercentCappedInspectionBundleAdjuster(var percent: Double) : CappedInspectionBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedInspectionBundleQuantityOffsetAdjuster(var weight: Double) : CappedInspectionBundleModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: CappedInspectionBundle, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the capped inspection bundle steps in order and keeps a trace of each. */
data class CappedInspectionBundleEngine(
    var steps: MutableList<CappedInspectionBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedInspectionBundle): Double {
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

    fun evaluateAll(items: MutableList<CappedInspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedInspectionBundleEngine(): CappedInspectionBundleEngine {
    return CappedInspectionBundleEngine(
        steps = mutableListOf<CappedInspectionBundleModifier>(
            TieredCappedInspectionBundleAdjuster(
                mutableListOf<Double>(1741.0, 3222.0, 5963.0),
                mutableListOf<Double>(0.98, 0.93, 1.19),
            ),
            MinimumCappedInspectionBundleAdjuster(6.9),
            RoundingCappedInspectionBundleAdjuster(0.1),
            PercentCappedInspectionBundleAdjuster(13.5),
            CappedInspectionBundleQuantityOffsetAdjuster(1.49),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 74167.1,
    )
}
