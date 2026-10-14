package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor digest calculation. */
interface EmissionFactorDigestAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorDigest, value: Double): Double
}

data class CapEmissionFactorDigestAdjuster(var cap: Double) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumEmissionFactorDigestAdjuster(var minimum: Double) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEmissionFactorDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEmissionFactorDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentEmissionFactorDigestAdjuster(var percent: Double) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceEmissionFactorDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorDigestAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: EmissionFactorDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor digest steps in order and keeps a trace of each. */
data class EmissionFactorDigestEngine(
    var steps: MutableList<EmissionFactorDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EmissionFactorDigest): Double {
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

    fun evaluateAll(items: MutableList<EmissionFactorDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEmissionFactorDigestEngine(flags: Registry): EmissionFactorDigestEngine {
    return EmissionFactorDigestEngine(
        steps = mutableListOf<EmissionFactorDigestAdjuster>(
            CapEmissionFactorDigestAdjuster(49749.0),
            MinimumEmissionFactorDigestAdjuster(29.6),
            FlaggedEmissionFactorDigestAdjuster(flags, "emission-factor-digest-tiered", -4.6),
            TieredEmissionFactorDigestAdjuster(
                mutableListOf<Double>(1055.0, 1626.0, 7561.0),
                mutableListOf<Double>(1.14, 1.15, 1.02),
            ),
            PercentEmissionFactorDigestAdjuster(-1.3),
            SourceEmissionFactorDigestAdjuster(
                mutableMapOf<String, Double>("central" to 1.11, "retail" to 1.39),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 28902.4,
    )
}
