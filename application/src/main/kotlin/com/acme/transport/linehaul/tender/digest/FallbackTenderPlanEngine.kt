package com.acme.transport.linehaul.tender.digest

/** One step of the fallback tender plan calculation. */
interface FallbackTenderPlanFactor {
    fun describe(): String
    fun adjust(item: FallbackTenderPlan, value: Double): Double
}

data class MinimumFallbackTenderPlanAdjuster(var minimum: Double) : FallbackTenderPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTenderPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FallbackTenderPlanStopCountOffsetAdjuster(var weight: Double) : FallbackTenderPlanFactor {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: FallbackTenderPlan, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class TieredFallbackTenderPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTenderPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTenderPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFallbackTenderPlanAdjuster(var cap: Double) : FallbackTenderPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackTenderPlan, value: Double): Double = minOf(value, cap)
}

data class PercentFallbackTenderPlanAdjuster(var percent: Double) : FallbackTenderPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTenderPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback tender plan steps in order and keeps a trace of each. */
data class FallbackTenderPlanEngine(
    var steps: MutableList<FallbackTenderPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackTenderPlan): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTenderPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTenderPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackTenderPlanEngine(): FallbackTenderPlanEngine {
    return FallbackTenderPlanEngine(
        steps = mutableListOf<FallbackTenderPlanFactor>(
            MinimumFallbackTenderPlanAdjuster(30.2),
            FallbackTenderPlanStopCountOffsetAdjuster(1.98),
            TieredFallbackTenderPlanAdjuster(
                mutableListOf<Double>(3269.0, 6501.0, 9707.0),
                mutableListOf<Double>(0.84, 0.84, 0.82),
            ),
            CapFallbackTenderPlanAdjuster(16319.0),
            PercentFallbackTenderPlanAdjuster(-23.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 59987.7,
    )
}
