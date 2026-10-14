package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the emission factor adjustment calculation. */
interface EmissionFactorAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorAdjustment, value: Double): Double
}

data class FlaggedEmissionFactorAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EmissionFactorAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEmissionFactorAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEmissionFactorAdjustmentAdjuster(var minimum: Double) : EmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EmissionFactorAdjustmentMarginOffsetAdjuster(var weight: Double) : EmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: EmissionFactorAdjustment, value: Double): Double = value + item.margin * weight
}

data class PercentEmissionFactorAdjustmentAdjuster(var percent: Double) : EmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EmissionFactorAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the emission factor adjustment steps in order and keeps a trace of each. */
data class EmissionFactorAdjustmentEngine(
    var steps: MutableList<EmissionFactorAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EmissionFactorAdjustment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EmissionFactorAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEmissionFactorAdjustmentEngine(flags: Registry): EmissionFactorAdjustmentEngine {
    return EmissionFactorAdjustmentEngine(
        steps = mutableListOf<EmissionFactorAdjustmentAdjuster>(
            FlaggedEmissionFactorAdjustmentAdjuster(flags, "emission-factor-adjustment-capped", -8.0),
            TieredEmissionFactorAdjustmentAdjuster(
                mutableListOf<Double>(24.0, 2778.0),
                mutableListOf<Double>(1.06, 0.99),
            ),
            MinimumEmissionFactorAdjustmentAdjuster(13.3),
            EmissionFactorAdjustmentMarginOffsetAdjuster(1.49),
            PercentEmissionFactorAdjustmentAdjuster(-23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 75676.9,
    )
}
