package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered locker metric calculation. */
interface TieredLockerMetricModifier {
    fun describe(): String
    fun adjust(item: TieredLockerMetric, value: Double): Double
}

data class TieredTieredLockerMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredLockerMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredLockerMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredLockerMetricAdjuster(var cap: Double) : TieredLockerMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredLockerMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingTieredLockerMetricAdjuster(var step: Double) : TieredLockerMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredLockerMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumTieredLockerMetricAdjuster(var minimum: Double) : TieredLockerMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredLockerMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered locker metric steps in order and keeps a trace of each. */
data class TieredLockerMetricEngine(
    var steps: MutableList<TieredLockerMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredLockerMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredLockerMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredLockerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredLockerMetricEngine(): TieredLockerMetricEngine {
    return TieredLockerMetricEngine(
        steps = mutableListOf<TieredLockerMetricModifier>(
            TieredTieredLockerMetricAdjuster(
                mutableListOf<Double>(5622.0, 6719.0, 9219.0),
                mutableListOf<Double>(1.01, 0.96, 1.16),
            ),
            CapTieredLockerMetricAdjuster(27660.0),
            RoundingTieredLockerMetricAdjuster(0.25),
            MinimumTieredLockerMetricAdjuster(31.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 30202.0,
    )
}
