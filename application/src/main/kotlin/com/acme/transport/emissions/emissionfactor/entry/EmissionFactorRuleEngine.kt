package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor rule calculation. */
interface EmissionFactorRuleModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorRule, value: Double): Double
}

data class PercentEmissionFactorRuleAdjuster(var percent: Double) : EmissionFactorRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class EmissionFactorRuleFuelLitersOffsetAdjuster(var weight: Double) : EmissionFactorRuleModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: EmissionFactorRule, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredEmissionFactorRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the emission factor rule steps in order and keeps a trace of each. */
data class EmissionFactorRuleEngine(
    var steps: MutableList<EmissionFactorRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorRule): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorRuleEngine(): EmissionFactorRuleEngine {
    return EmissionFactorRuleEngine(
        steps = mutableListOf<EmissionFactorRuleModifier>(
            PercentEmissionFactorRuleAdjuster(-11.6),
            EmissionFactorRuleFuelLitersOffsetAdjuster(1.3),
            TieredEmissionFactorRuleAdjuster(
                mutableListOf<Double>(2765.0, 5288.0),
                mutableListOf<Double>(1.06, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 95427.0,
    )
}
