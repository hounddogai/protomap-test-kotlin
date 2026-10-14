package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the emission factor quota calculation. */
interface EmissionFactorQuotaFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorQuota, value: Double): Double
}

data class FlaggedEmissionFactorQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class EmissionFactorQuotaCapacityOffsetAdjuster(var weight: Double) : EmissionFactorQuotaFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class PercentEmissionFactorQuotaAdjuster(var percent: Double) : EmissionFactorQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapEmissionFactorQuotaAdjuster(var cap: Double) : EmissionFactorQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double = minOf(value, cap)
}

data class LaneEmissionFactorQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorQuotaFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEmissionFactorQuotaAdjuster(var step: Double) : EmissionFactorQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EmissionFactorQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the emission factor quota steps in order and keeps a trace of each. */
data class EmissionFactorQuotaEngine(
    var steps: MutableList<EmissionFactorQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorQuota): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorQuotaEngine(flags: Registry): EmissionFactorQuotaEngine {
    return EmissionFactorQuotaEngine(
        steps = mutableListOf<EmissionFactorQuotaFactor>(
            FlaggedEmissionFactorQuotaAdjuster(flags, "emission-factor-quota-primary", -0.7),
            EmissionFactorQuotaCapacityOffsetAdjuster(1.13),
            PercentEmissionFactorQuotaAdjuster(-1.9),
            CapEmissionFactorQuotaAdjuster(47602.0),
            LaneEmissionFactorQuotaAdjuster(
                mutableMapOf<String, Double>("south" to 0.77, "central" to 0.88, "east" to 1.37),
            ),
            RoundingEmissionFactorQuotaAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 8691.0,
    )
}
