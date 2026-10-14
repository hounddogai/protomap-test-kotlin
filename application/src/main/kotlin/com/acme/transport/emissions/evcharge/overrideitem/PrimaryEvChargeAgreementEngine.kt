package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary ev charge agreement calculation. */
interface PrimaryEvChargeAgreementFactor {
    fun describe(): String
    fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double
}

data class RoundingPrimaryEvChargeAgreementAdjuster(var step: Double) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryEvChargeAgreementAdjuster(var minimum: Double) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapPrimaryEvChargeAgreementAdjuster(var cap: Double) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryEvChargeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPrimaryEvChargeAgreementAdjuster(var percent: Double) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryEvChargeAgreementAmountOffsetAdjuster(var weight: Double) : PrimaryEvChargeAgreementFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: PrimaryEvChargeAgreement, value: Double): Double = value + item.amount * weight
}

/** Runs the primary ev charge agreement steps in order and keeps a trace of each. */
data class PrimaryEvChargeAgreementEngine(
    var steps: MutableList<PrimaryEvChargeAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryEvChargeAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryEvChargeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryEvChargeAgreementEngine(): PrimaryEvChargeAgreementEngine {
    return PrimaryEvChargeAgreementEngine(
        steps = mutableListOf<PrimaryEvChargeAgreementFactor>(
            RoundingPrimaryEvChargeAgreementAdjuster(0.1),
            MinimumPrimaryEvChargeAgreementAdjuster(48.6),
            CapPrimaryEvChargeAgreementAdjuster(18443.0),
            TieredPrimaryEvChargeAgreementAdjuster(
                mutableListOf<Double>(3037.0, 5225.0, 6008.0),
                mutableListOf<Double>(0.95, 0.85, 1.13),
            ),
            PercentPrimaryEvChargeAgreementAdjuster(5.9),
            PrimaryEvChargeAgreementAmountOffsetAdjuster(0.83),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 38684.0,
    )
}
