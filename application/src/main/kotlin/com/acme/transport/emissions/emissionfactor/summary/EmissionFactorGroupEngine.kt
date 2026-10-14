package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor group calculation. */
interface EmissionFactorGroupAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorGroup, value: Double): Double
}

data class TieredEmissionFactorGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEmissionFactorGroupAdjuster(var minimum: Double) : EmissionFactorGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEmissionFactorGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor group steps in order and keeps a trace of each. */
data class EmissionFactorGroupEngine(
    var steps: MutableList<EmissionFactorGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EmissionFactorGroup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEmissionFactorGroupEngine(flags: Registry): EmissionFactorGroupEngine {
    return EmissionFactorGroupEngine(
        steps = mutableListOf<EmissionFactorGroupAdjuster>(
            TieredEmissionFactorGroupAdjuster(
                mutableListOf<Double>(1871.0, 7773.0),
                mutableListOf<Double>(0.97, 1.17),
            ),
            MinimumEmissionFactorGroupAdjuster(35.7),
            FlaggedEmissionFactorGroupAdjuster(flags, "emission-factor-group-lenient", -3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 69090.0,
    )
}
