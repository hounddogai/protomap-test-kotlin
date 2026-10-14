package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted rest break allocation calculation. */
interface WeightedRestBreakAllocationFactor {
    fun describe(): String
    fun adjust(item: WeightedRestBreakAllocation, value: Double): Double
}

data class WeightedRestBreakAllocationAmountOffsetAdjuster(
    var weight: Double,
) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double = value + item.amount * weight
}

data class CapWeightedRestBreakAllocationAdjuster(var cap: Double) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedRestBreakAllocationAdjuster(var step: Double) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double = roundTo(value, step)
}

data class RegionWeightedRestBreakAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedRestBreakAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedRestBreakAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedRestBreakAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedRestBreakAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted rest break allocation steps in order and keeps a trace of each. */
data class WeightedRestBreakAllocationEngine(
    var steps: MutableList<WeightedRestBreakAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedRestBreakAllocation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedRestBreakAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedRestBreakAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedRestBreakAllocationEngine(flags: Registry): WeightedRestBreakAllocationEngine {
    return WeightedRestBreakAllocationEngine(
        steps = mutableListOf<WeightedRestBreakAllocationFactor>(
            WeightedRestBreakAllocationAmountOffsetAdjuster(1.79),
            CapWeightedRestBreakAllocationAdjuster(9864.0),
            RoundingWeightedRestBreakAllocationAdjuster(0.05),
            RegionWeightedRestBreakAllocationAdjuster(mutableMapOf<String, Double>("online" to 0.91)),
            TieredWeightedRestBreakAllocationAdjuster(
                mutableListOf<Double>(941.0, 3895.0),
                mutableListOf<Double>(1.18, 1.14),
            ),
            FlaggedWeightedRestBreakAllocationAdjuster(flags, "weighted-rest-break-allocation-capped", 4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 51961.0,
    )
}
