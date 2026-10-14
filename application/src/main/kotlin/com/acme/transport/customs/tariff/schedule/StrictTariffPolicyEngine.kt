package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict tariff policy calculation. */
interface StrictTariffPolicyAdjuster {
    fun describe(): String
    fun adjust(item: StrictTariffPolicy, value: Double): Double
}

data class MinimumStrictTariffPolicyAdjuster(var minimum: Double) : StrictTariffPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTariffPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictTariffPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTariffPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTariffPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictTariffPolicyAdjuster(var step: Double) : StrictTariffPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTariffPolicy, value: Double): Double = roundTo(value, step)
}

data class StrictTariffPolicyThresholdOffsetAdjuster(var weight: Double) : StrictTariffPolicyAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: StrictTariffPolicy, value: Double): Double = value + item.threshold * weight
}

data class CapStrictTariffPolicyAdjuster(var cap: Double) : StrictTariffPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTariffPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the strict tariff policy steps in order and keeps a trace of each. */
data class StrictTariffPolicyEngine(
    var steps: MutableList<StrictTariffPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictTariffPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTariffPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTariffPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictTariffPolicyEngine(): StrictTariffPolicyEngine {
    return StrictTariffPolicyEngine(
        steps = mutableListOf<StrictTariffPolicyAdjuster>(
            MinimumStrictTariffPolicyAdjuster(6.5),
            TieredStrictTariffPolicyAdjuster(
                mutableListOf<Double>(1505.0, 3469.0, 6515.0, 7329.0),
                mutableListOf<Double>(1.13, 0.84, 0.86, 0.84),
            ),
            RoundingStrictTariffPolicyAdjuster(5.0),
            StrictTariffPolicyThresholdOffsetAdjuster(1.23),
            CapStrictTariffPolicyAdjuster(24652.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 29878.3,
    )
}
