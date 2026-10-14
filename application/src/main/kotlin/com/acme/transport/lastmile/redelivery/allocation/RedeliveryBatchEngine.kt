package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery batch calculation. */
interface RedeliveryBatchAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryBatch, value: Double): Double
}

data class FlaggedRedeliveryBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRedeliveryBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRedeliveryBatchAdjuster(var minimum: Double) : RedeliveryBatchAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameRedeliveryBatchAdjuster(var factors: MutableMap<String, Double>) : RedeliveryBatchAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RedeliveryBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRedeliveryBatchAdjuster(var step: Double) : RedeliveryBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryBatch, value: Double): Double = roundTo(value, step)
}

/** Runs the redelivery batch steps in order and keeps a trace of each. */
data class RedeliveryBatchEngine(
    var steps: MutableList<RedeliveryBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryBatch): Double {
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

    fun evaluateAll(items: MutableList<RedeliveryBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryBatchEngine(flags: Registry): RedeliveryBatchEngine {
    return RedeliveryBatchEngine(
        steps = mutableListOf<RedeliveryBatchAdjuster>(
            FlaggedRedeliveryBatchAdjuster(flags, "redelivery-batch-manual", 4.9),
            TieredRedeliveryBatchAdjuster(
                mutableListOf<Double>(2824.0, 2955.0),
                mutableListOf<Double>(0.97, 0.94),
            ),
            MinimumRedeliveryBatchAdjuster(2.2),
            NameRedeliveryBatchAdjuster(
                mutableMapOf<String, Double>("east" to 1.16, "north" to 1.4, "south" to 1.13),
            ),
            RoundingRedeliveryBatchAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 49535.5,
    )
}
