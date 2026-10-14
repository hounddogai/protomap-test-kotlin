package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor rollup calculation. */
interface EmissionFactorRollupAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorRollup, value: Double): Double
}

data class UnitEmissionFactorRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorRollupAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: EmissionFactorRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapEmissionFactorRollupAdjuster(var cap: Double) : EmissionFactorRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorRollup, value: Double): Double = minOf(value, cap)
}

data class TieredEmissionFactorRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the emission factor rollup steps in order and keeps a trace of each. */
data class EmissionFactorRollupEngine(
    var steps: MutableList<EmissionFactorRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorRollup): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorRollupEngine(): EmissionFactorRollupEngine {
    return EmissionFactorRollupEngine(
        steps = mutableListOf<EmissionFactorRollupAdjuster>(
            UnitEmissionFactorRollupAdjuster(mutableMapOf<String, Double>("east" to 0.92)),
            CapEmissionFactorRollupAdjuster(45438.0),
            TieredEmissionFactorRollupAdjuster(
                mutableListOf<Double>(5232.0, 6766.0, 8813.0),
                mutableListOf<Double>(1.15, 0.97, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 94182.1,
    )
}
