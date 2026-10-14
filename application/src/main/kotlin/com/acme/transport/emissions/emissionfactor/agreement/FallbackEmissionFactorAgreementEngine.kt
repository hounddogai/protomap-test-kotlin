package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback emission factor agreement calculation. */
interface FallbackEmissionFactorAgreementAdjuster {
    fun describe(): String
    fun adjust(item: FallbackEmissionFactorAgreement, value: Double): Double
}

data class FallbackEmissionFactorAgreementMinimumOffsetAdjuster(
    var weight: Double,
) : FallbackEmissionFactorAgreementAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: FallbackEmissionFactorAgreement, value: Double): Double = value + item.minimum * weight
}

data class TieredFallbackEmissionFactorAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackEmissionFactorAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackEmissionFactorAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumFallbackEmissionFactorAgreementAdjuster(
    var minimum: Double,
) : FallbackEmissionFactorAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackEmissionFactorAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback emission factor agreement steps in order and keeps a trace of each. */
data class FallbackEmissionFactorAgreementEngine(
    var steps: MutableList<FallbackEmissionFactorAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackEmissionFactorAgreement): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackEmissionFactorAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackEmissionFactorAgreementEngine(): FallbackEmissionFactorAgreementEngine {
    return FallbackEmissionFactorAgreementEngine(
        steps = mutableListOf<FallbackEmissionFactorAgreementAdjuster>(
            FallbackEmissionFactorAgreementMinimumOffsetAdjuster(0.44),
            TieredFallbackEmissionFactorAgreementAdjuster(
                mutableListOf<Double>(3839.0, 6867.0, 8238.0, 8984.0),
                mutableListOf<Double>(0.99, 0.87, 0.82, 1.18),
            ),
            MinimumFallbackEmissionFactorAgreementAdjuster(27.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 5334.8,
    )
}
