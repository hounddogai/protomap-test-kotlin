package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the locker adjustment calculation. */
interface LockerAdjustmentStep {
    fun describe(): String
    fun adjust(item: LockerAdjustment, value: Double): Double
}

data class PercentLockerAdjustmentAdjuster(var percent: Double) : LockerAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLockerAdjustmentAdjuster(var minimum: Double) : LockerAdjustmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLockerAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker adjustment steps in order and keeps a trace of each. */
data class LockerAdjustmentEngine(
    var steps: MutableList<LockerAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerAdjustment): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerAdjustmentEngine(): LockerAdjustmentEngine {
    return LockerAdjustmentEngine(
        steps = mutableListOf<LockerAdjustmentStep>(
            PercentLockerAdjustmentAdjuster(-24.9),
            MinimumLockerAdjustmentAdjuster(37.1),
            TieredLockerAdjustmentAdjuster(
                mutableListOf<Double>(6903.0, 6928.0, 9304.0),
                mutableListOf<Double>(0.88, 1.13, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 8526.7,
    )
}
