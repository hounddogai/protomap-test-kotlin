package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default emission factor adjustment calculation. */
interface DefaultEmissionFactorAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double
}

data class TieredDefaultEmissionFactorAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultEmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultEmissionFactorAdjustmentAdjuster(
    var minimum: Double,
) : DefaultEmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultEmissionFactorAdjustmentAdjuster(
    var step: Double,
) : DefaultEmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultEmissionFactorAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultEmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultEmissionFactorAdjustmentMinimumOffsetAdjuster(
    var weight: Double,
) : DefaultEmissionFactorAdjustmentAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: DefaultEmissionFactorAdjustment, value: Double): Double = value + item.minimum * weight
}

/** Runs the default emission factor adjustment steps in order and keeps a trace of each. */
data class DefaultEmissionFactorAdjustmentEngine(
    var steps: MutableList<DefaultEmissionFactorAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultEmissionFactorAdjustment): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultEmissionFactorAdjustmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultEmissionFactorAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultEmissionFactorAdjustmentEngine(flags: Registry): DefaultEmissionFactorAdjustmentEngine {
    return DefaultEmissionFactorAdjustmentEngine(
        steps = mutableListOf<DefaultEmissionFactorAdjustmentAdjuster>(
            TieredDefaultEmissionFactorAdjustmentAdjuster(
                mutableListOf<Double>(618.0, 1811.0, 7132.0, 7245.0),
                mutableListOf<Double>(0.81, 0.88, 0.85, 1.2),
            ),
            MinimumDefaultEmissionFactorAdjustmentAdjuster(24.7),
            RoundingDefaultEmissionFactorAdjustmentAdjuster(0.25),
            FlaggedDefaultEmissionFactorAdjustmentAdjuster(
                flags,
                "default-emission-factor-adjustment-default",
                -2.5,
            ),
            DefaultEmissionFactorAdjustmentMinimumOffsetAdjuster(1.27),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 96758.0,
    )
}
