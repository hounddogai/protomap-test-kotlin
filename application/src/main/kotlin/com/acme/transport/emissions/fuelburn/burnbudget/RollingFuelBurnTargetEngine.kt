package com.acme.transport.emissions.fuelburn.burnbudget

/** One step of the rolling fuel burn target calculation. */
interface RollingFuelBurnTargetStep {
    fun describe(): String
    fun adjust(item: RollingFuelBurnTarget, value: Double): Double
}

data class RollingFuelBurnTargetStartsAtOffsetAdjuster(var weight: Double) : RollingFuelBurnTargetStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RollingFuelBurnTarget, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapRollingFuelBurnTargetAdjuster(var cap: Double) : RollingFuelBurnTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelBurnTarget, value: Double): Double = minOf(value, cap)
}

data class TieredRollingFuelBurnTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingFuelBurnTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingFuelBurnTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling fuel burn target steps in order and keeps a trace of each. */
data class RollingFuelBurnTargetEngine(
    var steps: MutableList<RollingFuelBurnTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingFuelBurnTarget): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingFuelBurnTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<RollingFuelBurnTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnTargetEngine(): RollingFuelBurnTargetEngine {
    return RollingFuelBurnTargetEngine(
        steps = mutableListOf<RollingFuelBurnTargetStep>(
            RollingFuelBurnTargetStartsAtOffsetAdjuster(0.63),
            CapRollingFuelBurnTargetAdjuster(12381.0),
            TieredRollingFuelBurnTargetAdjuster(
                mutableListOf<Double>(557.0, 2072.0, 7211.0),
                mutableListOf<Double>(0.95, 1.15, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 45746.9,
    )
}
