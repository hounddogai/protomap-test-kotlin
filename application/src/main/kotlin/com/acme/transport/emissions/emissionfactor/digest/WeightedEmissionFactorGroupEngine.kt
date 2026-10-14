package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted emission factor group calculation. */
interface WeightedEmissionFactorGroupAdjuster {
    fun describe(): String
    fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double
}

data class TieredWeightedEmissionFactorGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedEmissionFactorGroupAdjuster(var step: Double) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double = roundTo(value, step)
}

data class LocaleWeightedEmissionFactorGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedEmissionFactorGroupAdjuster(var cap: Double) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedEmissionFactorGroupAdjuster(
    var minimum: Double,
) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedEmissionFactorGroupLimitOffsetAdjuster(
    var weight: Double,
) : WeightedEmissionFactorGroupAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: WeightedEmissionFactorGroup, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the weighted emission factor group steps in order and keeps a trace of each. */
data class WeightedEmissionFactorGroupEngine(
    var steps: MutableList<WeightedEmissionFactorGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedEmissionFactorGroup): Double {
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

    fun evaluateAll(items: MutableList<WeightedEmissionFactorGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedEmissionFactorGroupEngine(): WeightedEmissionFactorGroupEngine {
    return WeightedEmissionFactorGroupEngine(
        steps = mutableListOf<WeightedEmissionFactorGroupAdjuster>(
            TieredWeightedEmissionFactorGroupAdjuster(
                mutableListOf<Double>(6437.0, 8353.0),
                mutableListOf<Double>(0.97, 0.94),
            ),
            RoundingWeightedEmissionFactorGroupAdjuster(0.05),
            LocaleWeightedEmissionFactorGroupAdjuster(mutableMapOf<String, Double>("west" to 1.17)),
            CapWeightedEmissionFactorGroupAdjuster(38617.0),
            MinimumWeightedEmissionFactorGroupAdjuster(35.6),
            WeightedEmissionFactorGroupLimitOffsetAdjuster(0.73),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 49014.8,
    )
}
