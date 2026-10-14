package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict emission factor allocation calculation. */
interface StrictEmissionFactorAllocationFactor {
    fun describe(): String
    fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double
}

data class PercentStrictEmissionFactorAllocationAdjuster(
    var percent: Double,
) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictEmissionFactorAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleStrictEmissionFactorAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictEmissionFactorAllocationAdjuster(var cap: Double) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictEmissionFactorAllocationAdjuster(
    var minimum: Double,
) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictEmissionFactorAllocationPalletsOffsetAdjuster(
    var weight: Double,
) : StrictEmissionFactorAllocationFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: StrictEmissionFactorAllocation, value: Double): Double {
        return value + item.pallets.toDouble() * weight
    }
}

/** Runs the strict emission factor allocation steps in order and keeps a trace of each. */
data class StrictEmissionFactorAllocationEngine(
    var steps: MutableList<StrictEmissionFactorAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictEmissionFactorAllocation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictEmissionFactorAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictEmissionFactorAllocationEngine(): StrictEmissionFactorAllocationEngine {
    return StrictEmissionFactorAllocationEngine(
        steps = mutableListOf<StrictEmissionFactorAllocationFactor>(
            PercentStrictEmissionFactorAllocationAdjuster(-22.5),
            TieredStrictEmissionFactorAllocationAdjuster(
                mutableListOf<Double>(2450.0, 3719.0, 7771.0, 8294.0),
                mutableListOf<Double>(1.01, 0.93, 1.04, 0.8),
            ),
            TitleStrictEmissionFactorAllocationAdjuster(mutableMapOf<String, Double>("north" to 0.78)),
            CapStrictEmissionFactorAllocationAdjuster(31342.0),
            MinimumStrictEmissionFactorAllocationAdjuster(37.8),
            StrictEmissionFactorAllocationPalletsOffsetAdjuster(1.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 81289.2,
    )
}
