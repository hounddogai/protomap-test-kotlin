package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor mapping calculation. */
interface EmissionFactorMappingFactor {
    fun describe(): String
    fun adjust(item: EmissionFactorMapping, value: Double): Double
}

data class MinimumEmissionFactorMappingAdjuster(var minimum: Double) : EmissionFactorMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEmissionFactorMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorMappingFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneEmissionFactorMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorMappingFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: EmissionFactorMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentEmissionFactorMappingAdjuster(var percent: Double) : EmissionFactorMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor mapping steps in order and keeps a trace of each. */
data class EmissionFactorMappingEngine(
    var steps: MutableList<EmissionFactorMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorMapping): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorMappingEngine(flags: Registry): EmissionFactorMappingEngine {
    return EmissionFactorMappingEngine(
        steps = mutableListOf<EmissionFactorMappingFactor>(
            MinimumEmissionFactorMappingAdjuster(40.7),
            FlaggedEmissionFactorMappingAdjuster(flags, "emission-factor-mapping-default", 1.0),
            LaneEmissionFactorMappingAdjuster(mutableMapOf<String, Double>("north" to 0.93)),
            PercentEmissionFactorMappingAdjuster(20.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 49776.8,
    )
}
