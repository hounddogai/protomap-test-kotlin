package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the locker variance calculation. */
interface LockerVarianceStep {
    fun describe(): String
    fun adjust(item: LockerVariance, value: Double): Double
}

data class TieredLockerVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLockerVarianceAdjuster(var step: Double) : LockerVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerVariance, value: Double): Double = roundTo(value, step)
}

data class LockerVarianceCreatedAtOffsetAdjuster(var weight: Double) : LockerVarianceStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LockerVariance, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the locker variance steps in order and keeps a trace of each. */
data class LockerVarianceEngine(
    var steps: MutableList<LockerVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerVariance): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<LockerVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerVarianceEngine(): LockerVarianceEngine {
    return LockerVarianceEngine(
        steps = mutableListOf<LockerVarianceStep>(
            TieredLockerVarianceAdjuster(
                mutableListOf<Double>(23.0, 5979.0, 6517.0),
                mutableListOf<Double>(1.07, 1.07, 0.93),
            ),
            RoundingLockerVarianceAdjuster(0.1),
            LockerVarianceCreatedAtOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 55494.4,
    )
}
