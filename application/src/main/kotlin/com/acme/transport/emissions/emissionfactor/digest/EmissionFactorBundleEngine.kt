package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor bundle calculation. */
interface EmissionFactorBundleFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorBundle, value: Double): Double
}

data class CapEmissionFactorBundleAdjuster(var cap: Double) : EmissionFactorBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedEmissionFactorBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceEmissionFactorBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorBundleFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: EmissionFactorBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor bundle steps in order and keeps a trace of each. */
data class EmissionFactorBundleEngine(
    var steps: MutableList<EmissionFactorBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorBundle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorBundleEngine(flags: Registry): EmissionFactorBundleEngine {
    return EmissionFactorBundleEngine(
        steps = mutableListOf<EmissionFactorBundleFactor>(
            CapEmissionFactorBundleAdjuster(838.0),
            FlaggedEmissionFactorBundleAdjuster(flags, "emission-factor-bundle-primary", 2.9),
            SourceEmissionFactorBundleAdjuster(mutableMapOf<String, Double>("retail" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 89958.6,
    )
}
