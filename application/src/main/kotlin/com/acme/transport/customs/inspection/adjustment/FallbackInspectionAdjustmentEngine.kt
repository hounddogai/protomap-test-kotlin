package com.acme.transport.customs.inspection.adjustment

/** One step of the fallback inspection adjustment calculation. */
interface FallbackInspectionAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: FallbackInspectionAdjustment, value: Double): Double
}

data class TieredFallbackInspectionAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackInspectionAdjustmentBudgetOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionAdjustmentAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: FallbackInspectionAdjustment, value: Double): Double = value + item.budget * weight
}

data class CapFallbackInspectionAdjustmentAdjuster(var cap: Double) : FallbackInspectionAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionAdjustment, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback inspection adjustment steps in order and keeps a trace of each. */
data class FallbackInspectionAdjustmentEngine(
    var steps: MutableList<FallbackInspectionAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackInspectionAdjustment): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionAdjustmentAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackInspectionAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackInspectionAdjustmentEngine(): FallbackInspectionAdjustmentEngine {
    return FallbackInspectionAdjustmentEngine(
        steps = mutableListOf<FallbackInspectionAdjustmentAdjuster>(
            TieredFallbackInspectionAdjustmentAdjuster(
                mutableListOf<Double>(1670.0, 2714.0, 9060.0),
                mutableListOf<Double>(0.87, 1.05, 1.02),
            ),
            FallbackInspectionAdjustmentBudgetOffsetAdjuster(1.1),
            CapFallbackInspectionAdjustmentAdjuster(5215.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 43328.2,
    )
}
