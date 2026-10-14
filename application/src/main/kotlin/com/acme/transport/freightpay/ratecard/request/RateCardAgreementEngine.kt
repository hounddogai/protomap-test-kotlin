package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card agreement calculation. */
interface RateCardAgreementStep {
    fun describe(): String
    fun adjust(item: RateCardAgreement, value: Double): Double
}

data class CapRateCardAgreementAdjuster(var cap: Double) : RateCardAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardAgreement, value: Double): Double = minOf(value, cap)
}

data class MinimumRateCardAgreementAdjuster(var minimum: Double) : RateCardAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRateCardAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RateCardAgreementVersionOffsetAdjuster(var weight: Double) : RateCardAgreementStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RateCardAgreement, value: Double): Double = value + item.version.toDouble() * weight
}

/** Runs the rate card agreement steps in order and keeps a trace of each. */
data class RateCardAgreementEngine(
    var steps: MutableList<RateCardAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardAgreement): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardAgreementEngine(): RateCardAgreementEngine {
    return RateCardAgreementEngine(
        steps = mutableListOf<RateCardAgreementStep>(
            CapRateCardAgreementAdjuster(11295.0),
            MinimumRateCardAgreementAdjuster(36.0),
            TieredRateCardAgreementAdjuster(
                mutableListOf<Double>(1594.0, 4892.0, 7549.0, 8562.0),
                mutableListOf<Double>(1.01, 1.1, 1.14, 1.19),
            ),
            RateCardAgreementVersionOffsetAdjuster(1.07),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 87677.6,
    )
}
