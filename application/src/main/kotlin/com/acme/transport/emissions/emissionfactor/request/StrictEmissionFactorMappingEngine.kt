package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.flags.Registry

/** One step of the strict emission factor mapping calculation. */
interface StrictEmissionFactorMappingFactor {
    fun describe(): String
    fun adjust(item: StrictEmissionFactorMapping, value: Double): Double
}

data class PercentStrictEmissionFactorMappingAdjuster(var percent: Double) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictEmissionFactorMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictEmissionFactorMappingAdjuster(var minimum: Double) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictEmissionFactorMappingVersionOffsetAdjuster(
    var weight: Double,
) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class CapStrictEmissionFactorMappingAdjuster(var cap: Double) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double = minOf(value, cap)
}

data class FlaggedStrictEmissionFactorMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictEmissionFactorMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictEmissionFactorMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict emission factor mapping steps in order and keeps a trace of each. */
data class StrictEmissionFactorMappingEngine(
    var steps: MutableList<StrictEmissionFactorMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictEmissionFactorMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictEmissionFactorMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictEmissionFactorMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictEmissionFactorMappingEngine(flags: Registry): StrictEmissionFactorMappingEngine {
    return StrictEmissionFactorMappingEngine(
        steps = mutableListOf<StrictEmissionFactorMappingFactor>(
            PercentStrictEmissionFactorMappingAdjuster(-13.0),
            TieredStrictEmissionFactorMappingAdjuster(
                mutableListOf<Double>(1378.0, 5613.0, 5774.0),
                mutableListOf<Double>(0.9, 0.88, 0.84),
            ),
            MinimumStrictEmissionFactorMappingAdjuster(28.3),
            StrictEmissionFactorMappingVersionOffsetAdjuster(0.68),
            CapStrictEmissionFactorMappingAdjuster(8398.0),
            FlaggedStrictEmissionFactorMappingAdjuster(flags, "strict-emission-factor-mapping-default", -9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 11749.6,
    )
}
