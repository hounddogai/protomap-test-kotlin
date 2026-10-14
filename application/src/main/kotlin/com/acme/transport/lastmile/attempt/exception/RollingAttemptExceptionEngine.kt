package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling attempt exception calculation. */
interface RollingAttemptExceptionFactor {
    fun describe(): String
    fun adjust(item: RollingAttemptException, value: Double): Double
}

data class RollingAttemptExceptionLoadKgOffsetAdjuster(var weight: Double) : RollingAttemptExceptionFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RollingAttemptException, value: Double): Double = value + item.loadKg * weight
}

data class RoundingRollingAttemptExceptionAdjuster(var step: Double) : RollingAttemptExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingAttemptException, value: Double): Double = roundTo(value, step)
}

data class CapRollingAttemptExceptionAdjuster(var cap: Double) : RollingAttemptExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAttemptException, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling attempt exception steps in order and keeps a trace of each. */
data class RollingAttemptExceptionEngine(
    var steps: MutableList<RollingAttemptExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingAttemptException): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingAttemptExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingAttemptException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingAttemptExceptionEngine(): RollingAttemptExceptionEngine {
    return RollingAttemptExceptionEngine(
        steps = mutableListOf<RollingAttemptExceptionFactor>(
            RollingAttemptExceptionLoadKgOffsetAdjuster(1.06),
            RoundingRollingAttemptExceptionAdjuster(5.0),
            CapRollingAttemptExceptionAdjuster(47052.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 3071.1,
    )
}
