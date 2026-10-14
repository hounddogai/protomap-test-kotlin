package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break exception calculation. */
interface RestBreakExceptionStep {
    fun describe(): String
    fun adjust(item: RestBreakException, value: Double): Double
}

data class TieredRestBreakExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakExceptionStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RestBreakExceptionStartsAtOffsetAdjuster(var weight: Double) : RestBreakExceptionStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RestBreakException, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class RoundingRestBreakExceptionAdjuster(var step: Double) : RestBreakExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakException, value: Double): Double = roundTo(value, step)
}

data class LabelRestBreakExceptionAdjuster(var factors: MutableMap<String, Double>) : RestBreakExceptionStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RestBreakException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRestBreakExceptionAdjuster(var percent: Double) : RestBreakExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rest break exception steps in order and keeps a trace of each. */
data class RestBreakExceptionEngine(
    var steps: MutableList<RestBreakExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakExceptionEngine(): RestBreakExceptionEngine {
    return RestBreakExceptionEngine(
        steps = mutableListOf<RestBreakExceptionStep>(
            TieredRestBreakExceptionAdjuster(
                mutableListOf<Double>(4137.0, 6670.0, 6863.0),
                mutableListOf<Double>(1.05, 0.92, 0.99),
            ),
            RestBreakExceptionStartsAtOffsetAdjuster(2.0),
            RoundingRestBreakExceptionAdjuster(0.5),
            LabelRestBreakExceptionAdjuster(mutableMapOf<String, Double>("north" to 0.83, "south" to 0.93)),
            PercentRestBreakExceptionAdjuster(8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 8402.4,
    )
}
