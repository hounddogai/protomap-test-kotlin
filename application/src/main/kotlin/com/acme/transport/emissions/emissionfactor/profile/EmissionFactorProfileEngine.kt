package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.flags.Registry

/** One step of the emission factor profile calculation. */
interface EmissionFactorProfileAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorProfile, value: Double): Double
}

data class MinimumEmissionFactorProfileAdjuster(var minimum: Double) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitEmissionFactorProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedEmissionFactorProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEmissionFactorProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapEmissionFactorProfileAdjuster(var cap: Double) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double = minOf(value, cap)
}

data class PercentEmissionFactorProfileAdjuster(var percent: Double) : EmissionFactorProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor profile steps in order and keeps a trace of each. */
data class EmissionFactorProfileEngine(
    var steps: MutableList<EmissionFactorProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorProfile): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorProfileEngine(flags: Registry): EmissionFactorProfileEngine {
    return EmissionFactorProfileEngine(
        steps = mutableListOf<EmissionFactorProfileAdjuster>(
            MinimumEmissionFactorProfileAdjuster(37.5),
            UnitEmissionFactorProfileAdjuster(mutableMapOf<String, Double>("online" to 0.98, "north" to 1.15)),
            FlaggedEmissionFactorProfileAdjuster(flags, "emission-factor-profile-batched", -0.6),
            TieredEmissionFactorProfileAdjuster(
                mutableListOf<Double>(2312.0, 6706.0),
                mutableListOf<Double>(0.98, 0.86),
            ),
            CapEmissionFactorProfileAdjuster(14957.0),
            PercentEmissionFactorProfileAdjuster(8.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 26261.7,
    )
}
