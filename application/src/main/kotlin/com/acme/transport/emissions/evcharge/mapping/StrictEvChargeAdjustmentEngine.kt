package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict ev charge adjustment calculation. */
interface StrictEvChargeAdjustmentStep {
    fun describe(): String
    fun adjust(item: StrictEvChargeAdjustment, value: Double): Double
}

data class TieredStrictEvChargeAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEvChargeAdjustmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEvChargeAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictEvChargeAdjustmentGraceDaysOffsetAdjuster(var weight: Double) : StrictEvChargeAdjustmentStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: StrictEvChargeAdjustment, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class PercentStrictEvChargeAdjustmentAdjuster(var percent: Double) : StrictEvChargeAdjustmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictEvChargeAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict ev charge adjustment steps in order and keeps a trace of each. */
data class StrictEvChargeAdjustmentEngine(
    var steps: MutableList<StrictEvChargeAdjustmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictEvChargeAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictEvChargeAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictEvChargeAdjustmentEngine(): StrictEvChargeAdjustmentEngine {
    return StrictEvChargeAdjustmentEngine(
        steps = mutableListOf<StrictEvChargeAdjustmentStep>(
            TieredStrictEvChargeAdjustmentAdjuster(
                mutableListOf<Double>(886.0, 887.0, 3452.0, 5032.0),
                mutableListOf<Double>(1.07, 1.17, 0.94, 0.84),
            ),
            StrictEvChargeAdjustmentGraceDaysOffsetAdjuster(0.63),
            PercentStrictEvChargeAdjustmentAdjuster(19.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 70868.0,
    )
}
