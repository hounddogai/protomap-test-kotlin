package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the emission factor policy calculation. */
interface EmissionFactorPolicyStep {
    fun describe(): String
    fun adjust(item: EmissionFactorPolicy, value: Double): Double
}

data class EmissionFactorPolicyThresholdOffsetAdjuster(var weight: Double) : EmissionFactorPolicyStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: EmissionFactorPolicy, value: Double): Double = value + item.threshold * weight
}

data class RoundingEmissionFactorPolicyAdjuster(var step: Double) : EmissionFactorPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedEmissionFactorPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the emission factor policy steps in order and keeps a trace of each. */
data class EmissionFactorPolicyEngine(
    var steps: MutableList<EmissionFactorPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorPolicy): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorPolicyEngine(flags: Registry): EmissionFactorPolicyEngine {
    return EmissionFactorPolicyEngine(
        steps = mutableListOf<EmissionFactorPolicyStep>(
            EmissionFactorPolicyThresholdOffsetAdjuster(0.23),
            RoundingEmissionFactorPolicyAdjuster(0.5),
            FlaggedEmissionFactorPolicyAdjuster(flags, "emission-factor-policy-primary", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 4678.3,
    )
}
