package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual accessorial adjustment calculation. */
interface ManualAccessorialAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: ManualAccessorialAdjustment, value: Double): Double
}

data class TieredManualAccessorialAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAccessorialAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAccessorialAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualAccessorialAdjustmentAdjuster(
    var minimum: Double,
) : ManualAccessorialAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAccessorialAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentManualAccessorialAdjustmentAdjuster(
    var percent: Double,
) : ManualAccessorialAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualAccessorialAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualAccessorialAdjustmentAdjuster(var step: Double) : ManualAccessorialAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualAccessorialAdjustment, value: Double): Double = roundTo(value, step)
}

data class ManualAccessorialAdjustmentCapacityOffsetAdjuster(
    var weight: Double,
) : ManualAccessorialAdjustmentAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ManualAccessorialAdjustment, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

/** Runs the manual accessorial adjustment steps in order and keeps a trace of each. */
data class ManualAccessorialAdjustmentEngine(
    var steps: MutableList<ManualAccessorialAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualAccessorialAdjustment): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAccessorialAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualAccessorialAdjustmentEngine(): ManualAccessorialAdjustmentEngine {
    return ManualAccessorialAdjustmentEngine(
        steps = mutableListOf<ManualAccessorialAdjustmentAdjuster>(
            TieredManualAccessorialAdjustmentAdjuster(
                mutableListOf<Double>(857.0, 1102.0, 3913.0, 7646.0),
                mutableListOf<Double>(0.86, 1.02, 1.13, 1.09),
            ),
            MinimumManualAccessorialAdjustmentAdjuster(41.2),
            PercentManualAccessorialAdjustmentAdjuster(1.7),
            RoundingManualAccessorialAdjustmentAdjuster(0.5),
            ManualAccessorialAdjustmentCapacityOffsetAdjuster(0.01),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 5550.2,
    )
}
