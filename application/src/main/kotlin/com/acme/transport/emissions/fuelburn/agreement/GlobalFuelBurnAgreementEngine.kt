package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global fuel burn agreement calculation. */
interface GlobalFuelBurnAgreementStep {
    fun describe(): String
    fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double
}

data class RoundingGlobalFuelBurnAgreementAdjuster(var step: Double) : GlobalFuelBurnAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalFuelBurnAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalFuelBurnAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalFuelBurnAgreementAdjuster(var percent: Double) : GlobalFuelBurnAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalFuelBurnAgreementAdjuster(var minimum: Double) : GlobalFuelBurnAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class GlobalFuelBurnAgreementAttemptsOffsetAdjuster(var weight: Double) : GlobalFuelBurnAgreementStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: GlobalFuelBurnAgreement, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the global fuel burn agreement steps in order and keeps a trace of each. */
data class GlobalFuelBurnAgreementEngine(
    var steps: MutableList<GlobalFuelBurnAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalFuelBurnAgreement): Double {
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

    fun evaluateAll(items: MutableList<GlobalFuelBurnAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalFuelBurnAgreementEngine(): GlobalFuelBurnAgreementEngine {
    return GlobalFuelBurnAgreementEngine(
        steps = mutableListOf<GlobalFuelBurnAgreementStep>(
            RoundingGlobalFuelBurnAgreementAdjuster(0.25),
            TieredGlobalFuelBurnAgreementAdjuster(
                mutableListOf<Double>(428.0, 3230.0, 3862.0, 9204.0),
                mutableListOf<Double>(0.85, 1.16, 1.19, 0.83),
            ),
            PercentGlobalFuelBurnAgreementAdjuster(-22.0),
            MinimumGlobalFuelBurnAgreementAdjuster(4.8),
            GlobalFuelBurnAgreementAttemptsOffsetAdjuster(0.11),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 27804.8,
    )
}
