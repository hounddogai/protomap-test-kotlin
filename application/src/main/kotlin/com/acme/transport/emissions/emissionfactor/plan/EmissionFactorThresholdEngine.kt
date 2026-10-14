package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor threshold calculation. */
interface EmissionFactorThresholdModifier {
    fun describe(): String
    fun adjust(item: EmissionFactorThreshold, value: Double): Double
}

data class FlaggedEmissionFactorThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentEmissionFactorThresholdAdjuster(var percent: Double) : EmissionFactorThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingEmissionFactorThresholdAdjuster(var step: Double) : EmissionFactorThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorThreshold, value: Double): Double = roundTo(value, step)
}

data class TieredEmissionFactorThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the emission factor threshold steps in order and keeps a trace of each. */
data class EmissionFactorThresholdEngine(
    var steps: MutableList<EmissionFactorThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorThreshold): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorThresholdEngine(flags: Registry): EmissionFactorThresholdEngine {
    return EmissionFactorThresholdEngine(
        steps = mutableListOf<EmissionFactorThresholdModifier>(
            FlaggedEmissionFactorThresholdAdjuster(flags, "emission-factor-threshold-tiered", -8.8),
            PercentEmissionFactorThresholdAdjuster(-24.8),
            RoundingEmissionFactorThresholdAdjuster(0.5),
            TieredEmissionFactorThresholdAdjuster(
                mutableListOf<Double>(2918.0, 6674.0, 9229.0),
                mutableListOf<Double>(1.11, 1.17, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 24148.3,
    )
}
