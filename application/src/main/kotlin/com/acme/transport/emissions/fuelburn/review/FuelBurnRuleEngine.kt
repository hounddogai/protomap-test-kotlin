package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn rule calculation. */
interface FuelBurnRuleAdjuster {
    fun describe(): String
    fun adjust(item: FuelBurnRule, value: Double): Double
}

data class RoundingFuelBurnRuleAdjuster(var step: Double) : FuelBurnRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnRule, value: Double): Double = roundTo(value, step)
}

data class TieredFuelBurnRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFuelBurnRuleAdjuster(var cap: Double) : FuelBurnRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnRule, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn rule steps in order and keeps a trace of each. */
data class FuelBurnRuleEngine(
    var steps: MutableList<FuelBurnRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnRuleEngine(): FuelBurnRuleEngine {
    return FuelBurnRuleEngine(
        steps = mutableListOf<FuelBurnRuleAdjuster>(
            RoundingFuelBurnRuleAdjuster(0.5),
            TieredFuelBurnRuleAdjuster(
                mutableListOf<Double>(300.0, 1262.0, 2263.0, 3445.0),
                mutableListOf<Double>(1.16, 0.96, 0.93, 1.02),
            ),
            CapFuelBurnRuleAdjuster(35683.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 93118.9,
    )
}
