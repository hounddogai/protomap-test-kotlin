package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback fuel surcharge milestone calculation. */
interface FallbackFuelSurchargeMilestoneModifier {
    fun describe(): String
    fun adjust(item: FallbackFuelSurchargeMilestone, value: Double): Double
}

data class PercentFallbackFuelSurchargeMilestoneAdjuster(
    var percent: Double,
) : FallbackFuelSurchargeMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackFuelSurchargeMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackFuelSurchargeMilestoneAdjuster(var cap: Double) : FallbackFuelSurchargeMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackFuelSurchargeMilestone, value: Double): Double = minOf(value, cap)
}

data class FallbackFuelSurchargeMilestoneDistanceKmOffsetAdjuster(
    var weight: Double,
) : FallbackFuelSurchargeMilestoneModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: FallbackFuelSurchargeMilestone, value: Double): Double {
        return value + item.distanceKm * weight
    }
}

/** Runs the fallback fuel surcharge milestone steps in order and keeps a trace of each. */
data class FallbackFuelSurchargeMilestoneEngine(
    var steps: MutableList<FallbackFuelSurchargeMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackFuelSurchargeMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackFuelSurchargeMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackFuelSurchargeMilestoneEngine(): FallbackFuelSurchargeMilestoneEngine {
    return FallbackFuelSurchargeMilestoneEngine(
        steps = mutableListOf<FallbackFuelSurchargeMilestoneModifier>(
            PercentFallbackFuelSurchargeMilestoneAdjuster(15.1),
            CapFallbackFuelSurchargeMilestoneAdjuster(41684.0),
            FallbackFuelSurchargeMilestoneDistanceKmOffsetAdjuster(0.17),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 68182.6,
    )
}
