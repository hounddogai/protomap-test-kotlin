package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive emission factor mapping calculation. */
interface AdaptiveEmissionFactorMappingStep {
    fun describe(): String
    fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double
}

data class CapAdaptiveEmissionFactorMappingAdjuster(var cap: Double) : AdaptiveEmissionFactorMappingStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double = minOf(value, cap)
}

data class RegionAdaptiveEmissionFactorMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveEmissionFactorMappingStep {
    override fun describe(): String = "by region"

    override fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveEmissionFactorMappingAdjuster(var step: Double) : AdaptiveEmissionFactorMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveEmissionFactorMappingAdjuster(
    var percent: Double,
) : AdaptiveEmissionFactorMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveEmissionFactorMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveEmissionFactorMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveEmissionFactorMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive emission factor mapping steps in order and keeps a trace of each. */
data class AdaptiveEmissionFactorMappingEngine(
    var steps: MutableList<AdaptiveEmissionFactorMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveEmissionFactorMapping): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveEmissionFactorMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveEmissionFactorMappingEngine(flags: Registry): AdaptiveEmissionFactorMappingEngine {
    return AdaptiveEmissionFactorMappingEngine(
        steps = mutableListOf<AdaptiveEmissionFactorMappingStep>(
            CapAdaptiveEmissionFactorMappingAdjuster(46903.0),
            RegionAdaptiveEmissionFactorMappingAdjuster(
                mutableMapOf<String, Double>("north" to 0.75, "west" to 1.14, "east" to 1.02),
            ),
            RoundingAdaptiveEmissionFactorMappingAdjuster(5.0),
            PercentAdaptiveEmissionFactorMappingAdjuster(2.7),
            FlaggedAdaptiveEmissionFactorMappingAdjuster(
                flags,
                "adaptive-emission-factor-mapping-regional",
                -0.8,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 37731.6,
    )
}
