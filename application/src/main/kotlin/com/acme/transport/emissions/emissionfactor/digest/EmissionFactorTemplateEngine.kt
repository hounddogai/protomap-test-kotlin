package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor template calculation. */
interface EmissionFactorTemplateModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorTemplate, value: Double): Double
}

data class MinimumEmissionFactorTemplateAdjuster(var minimum: Double) : EmissionFactorTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredEmissionFactorTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedEmissionFactorTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorTemplateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor template steps in order and keeps a trace of each. */
data class EmissionFactorTemplateEngine(
    var steps: MutableList<EmissionFactorTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorTemplate): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorTemplateEngine(flags: Registry): EmissionFactorTemplateEngine {
    return EmissionFactorTemplateEngine(
        steps = mutableListOf<EmissionFactorTemplateModifier>(
            MinimumEmissionFactorTemplateAdjuster(14.0),
            TieredEmissionFactorTemplateAdjuster(
                mutableListOf<Double>(1594.0, 2633.0),
                mutableListOf<Double>(1.05, 0.85),
            ),
            FlaggedEmissionFactorTemplateAdjuster(flags, "emission-factor-template-rolling", 10.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 9760.6,
    )
}
