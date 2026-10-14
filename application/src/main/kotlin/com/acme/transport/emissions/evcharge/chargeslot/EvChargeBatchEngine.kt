package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge batch calculation. */
interface EvChargeBatchFactor {
    fun describe(): String
    fun adjust(item: EvChargeBatch, value: Double): Double
}

data class RegionEvChargeBatchAdjuster(var factors: MutableMap<String, Double>) : EvChargeBatchFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: EvChargeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredEvChargeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingEvChargeBatchAdjuster(var step: Double) : EvChargeBatchFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumEvChargeBatchAdjuster(var minimum: Double) : EvChargeBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentEvChargeBatchAdjuster(var percent: Double) : EvChargeBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the ev charge batch steps in order and keeps a trace of each. */
data class EvChargeBatchEngine(
    var steps: MutableList<EvChargeBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeBatch): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeBatchFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeBatchEngine(): EvChargeBatchEngine {
    return EvChargeBatchEngine(
        steps = mutableListOf<EvChargeBatchFactor>(
            RegionEvChargeBatchAdjuster(mutableMapOf<String, Double>("north" to 1.04)),
            TieredEvChargeBatchAdjuster(
                mutableListOf<Double>(1139.0, 1707.0, 3961.0),
                mutableListOf<Double>(0.98, 1.0, 1.18),
            ),
            RoundingEvChargeBatchAdjuster(0.25),
            MinimumEvChargeBatchAdjuster(2.8),
            PercentEvChargeBatchAdjuster(-14.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 71586.7,
    )
}
