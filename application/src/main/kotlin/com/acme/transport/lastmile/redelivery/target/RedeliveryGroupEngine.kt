package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery group calculation. */
interface RedeliveryGroupFactor {
    fun describe(): String
    fun adjust(item: RedeliveryGroup, value: Double): Double
}

data class PercentRedeliveryGroupAdjuster(var percent: Double) : RedeliveryGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameRedeliveryGroupAdjuster(var factors: MutableMap<String, Double>) : RedeliveryGroupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RedeliveryGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RedeliveryGroupVersionOffsetAdjuster(var weight: Double) : RedeliveryGroupFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RedeliveryGroup, value: Double): Double = value + item.version.toDouble() * weight
}

data class RoundingRedeliveryGroupAdjuster(var step: Double) : RedeliveryGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryGroup, value: Double): Double = roundTo(value, step)
}

data class MinimumRedeliveryGroupAdjuster(var minimum: Double) : RedeliveryGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery group steps in order and keeps a trace of each. */
data class RedeliveryGroupEngine(
    var steps: MutableList<RedeliveryGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryGroup): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryGroupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryGroupEngine(): RedeliveryGroupEngine {
    return RedeliveryGroupEngine(
        steps = mutableListOf<RedeliveryGroupFactor>(
            PercentRedeliveryGroupAdjuster(5.5),
            NameRedeliveryGroupAdjuster(mutableMapOf<String, Double>("retail" to 1.02)),
            RedeliveryGroupVersionOffsetAdjuster(1.09),
            RoundingRedeliveryGroupAdjuster(0.25),
            MinimumRedeliveryGroupAdjuster(6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 4881.6,
    )
}
