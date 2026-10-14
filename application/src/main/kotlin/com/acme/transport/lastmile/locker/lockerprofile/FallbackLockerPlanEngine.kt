package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback locker plan calculation. */
interface FallbackLockerPlanFactor {
    fun describe(): String
    fun adjust(item: FallbackLockerPlan, value: Double): Double
}

data class FallbackLockerPlanMaximumOffsetAdjuster(var weight: Double) : FallbackLockerPlanFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: FallbackLockerPlan, value: Double): Double = value + item.maximum * weight
}

data class CapFallbackLockerPlanAdjuster(var cap: Double) : FallbackLockerPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackLockerPlan, value: Double): Double = minOf(value, cap)
}

data class MinimumFallbackLockerPlanAdjuster(var minimum: Double) : FallbackLockerPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackLockerPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackLockerPlanAdjuster(var step: Double) : FallbackLockerPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLockerPlan, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackLockerPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLockerPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLockerPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback locker plan steps in order and keeps a trace of each. */
data class FallbackLockerPlanEngine(
    var steps: MutableList<FallbackLockerPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackLockerPlan): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackLockerPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackLockerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackLockerPlanEngine(): FallbackLockerPlanEngine {
    return FallbackLockerPlanEngine(
        steps = mutableListOf<FallbackLockerPlanFactor>(
            FallbackLockerPlanMaximumOffsetAdjuster(1.9),
            CapFallbackLockerPlanAdjuster(35235.0),
            MinimumFallbackLockerPlanAdjuster(1.5),
            RoundingFallbackLockerPlanAdjuster(1.0),
            TieredFallbackLockerPlanAdjuster(
                mutableListOf<Double>(4384.0, 6433.0, 7993.0),
                mutableListOf<Double>(1.08, 1.11, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 60838.4,
    )
}
