package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict ev charge claim calculation. */
interface StrictEvChargeClaimAdjuster {
    fun describe(): String
    fun adjust(item: StrictEvChargeClaim, value: Double): Double
}

data class TieredStrictEvChargeClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEvChargeClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEvChargeClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictEvChargeClaimAdjuster(var step: Double) : StrictEvChargeClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictEvChargeClaim, value: Double): Double = roundTo(value, step)
}

data class CapStrictEvChargeClaimAdjuster(var cap: Double) : StrictEvChargeClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictEvChargeClaim, value: Double): Double = minOf(value, cap)
}

data class PercentStrictEvChargeClaimAdjuster(var percent: Double) : StrictEvChargeClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictEvChargeClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict ev charge claim steps in order and keeps a trace of each. */
data class StrictEvChargeClaimEngine(
    var steps: MutableList<StrictEvChargeClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictEvChargeClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictEvChargeClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictEvChargeClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictEvChargeClaimEngine(): StrictEvChargeClaimEngine {
    return StrictEvChargeClaimEngine(
        steps = mutableListOf<StrictEvChargeClaimAdjuster>(
            TieredStrictEvChargeClaimAdjuster(
                mutableListOf<Double>(6564.0, 9947.0),
                mutableListOf<Double>(1.11, 0.99),
            ),
            RoundingStrictEvChargeClaimAdjuster(5.0),
            CapStrictEvChargeClaimAdjuster(723.0),
            PercentStrictEvChargeClaimAdjuster(11.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 30538.4,
    )
}
