package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn agreement calculation. */
interface FuelBurnAgreementFactor {
    fun describe(): String
    fun adjust(item: FuelBurnAgreement, value: Double): Double
}

data class FlaggedFuelBurnAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnAgreementFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFuelBurnAgreementAdjuster(var step: Double) : FuelBurnAgreementFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnAgreement, value: Double): Double = roundTo(value, step)
}

data class FuelBurnAgreementEtaMinutesOffsetAdjuster(var weight: Double) : FuelBurnAgreementFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: FuelBurnAgreement, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the fuel burn agreement steps in order and keeps a trace of each. */
data class FuelBurnAgreementEngine(
    var steps: MutableList<FuelBurnAgreementFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnAgreement): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnAgreementEngine(flags: Registry): FuelBurnAgreementEngine {
    return FuelBurnAgreementEngine(
        steps = mutableListOf<FuelBurnAgreementFactor>(
            FlaggedFuelBurnAgreementAdjuster(flags, "fuel-burn-agreement-seasonal", -3.2),
            RoundingFuelBurnAgreementAdjuster(0.1),
            FuelBurnAgreementEtaMinutesOffsetAdjuster(1.81),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 20680.3,
    )
}
