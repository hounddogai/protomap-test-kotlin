package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor override calculation. */
interface EmissionFactorOverrideStep {
    fun describe(): String
    fun adjust(item: EmissionFactorOverride, value: Double): Double
}

data class PercentEmissionFactorOverrideAdjuster(var percent: Double) : EmissionFactorOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEmissionFactorOverrideAdjuster(var cap: Double) : EmissionFactorOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedEmissionFactorOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingEmissionFactorOverrideAdjuster(var step: Double) : EmissionFactorOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumEmissionFactorOverrideAdjuster(var minimum: Double) : EmissionFactorOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the emission factor override steps in order and keeps a trace of each. */
data class EmissionFactorOverrideEngine(
    var steps: MutableList<EmissionFactorOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorOverrideEngine(flags: Registry): EmissionFactorOverrideEngine {
    return EmissionFactorOverrideEngine(
        steps = mutableListOf<EmissionFactorOverrideStep>(
            PercentEmissionFactorOverrideAdjuster(-17.4),
            CapEmissionFactorOverrideAdjuster(27384.0),
            FlaggedEmissionFactorOverrideAdjuster(flags, "emission-factor-override-manual", -6.4),
            RoundingEmissionFactorOverrideAdjuster(0.5),
            MinimumEmissionFactorOverrideAdjuster(15.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 58731.8,
    )
}
