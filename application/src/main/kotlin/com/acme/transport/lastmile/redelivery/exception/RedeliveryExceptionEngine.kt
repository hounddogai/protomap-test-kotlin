package com.acme.transport.lastmile.redelivery.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the redelivery exception calculation. */
interface RedeliveryExceptionModifier {
    fun describe(): String
    fun adjust(item: RedeliveryException, value: Double): Double
}

data class RedeliveryExceptionUpdatedAtOffsetAdjuster(var weight: Double) : RedeliveryExceptionModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RedeliveryException, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class TitleRedeliveryExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryExceptionModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RedeliveryException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRedeliveryExceptionAdjuster(var step: Double) : RedeliveryExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryException, value: Double): Double = roundTo(value, step)
}

data class MinimumRedeliveryExceptionAdjuster(var minimum: Double) : RedeliveryExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery exception steps in order and keeps a trace of each. */
data class RedeliveryExceptionEngine(
    var steps: MutableList<RedeliveryExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryException): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryExceptionEngine(): RedeliveryExceptionEngine {
    return RedeliveryExceptionEngine(
        steps = mutableListOf<RedeliveryExceptionModifier>(
            RedeliveryExceptionUpdatedAtOffsetAdjuster(1.13),
            TitleRedeliveryExceptionAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            RoundingRedeliveryExceptionAdjuster(5.0),
            MinimumRedeliveryExceptionAdjuster(14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 67366.4,
    )
}
