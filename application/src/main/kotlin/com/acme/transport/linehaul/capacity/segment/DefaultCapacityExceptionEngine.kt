package com.acme.transport.linehaul.capacity.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the default capacity exception calculation. */
interface DefaultCapacityExceptionAdjuster {
    fun describe(): String
    fun adjust(item: DefaultCapacityException, value: Double): Double
}

data class PercentDefaultCapacityExceptionAdjuster(var percent: Double) : DefaultCapacityExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCapacityException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceDefaultCapacityExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultCapacityExceptionAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DefaultCapacityException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDefaultCapacityExceptionAdjuster(var step: Double) : DefaultCapacityExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCapacityException, value: Double): Double = roundTo(value, step)
}

/** Runs the default capacity exception steps in order and keeps a trace of each. */
data class DefaultCapacityExceptionEngine(
    var steps: MutableList<DefaultCapacityExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCapacityException): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCapacityExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCapacityException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCapacityExceptionEngine(): DefaultCapacityExceptionEngine {
    return DefaultCapacityExceptionEngine(
        steps = mutableListOf<DefaultCapacityExceptionAdjuster>(
            PercentDefaultCapacityExceptionAdjuster(-21.0),
            SourceDefaultCapacityExceptionAdjuster(
                mutableMapOf<String, Double>("south" to 1.33, "online" to 1.05),
            ),
            RoundingDefaultCapacityExceptionAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 58335.1,
    )
}
