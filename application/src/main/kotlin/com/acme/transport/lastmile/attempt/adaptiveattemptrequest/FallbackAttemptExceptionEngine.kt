package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.flags.Registry

/** One step of the fallback attempt exception calculation. */
interface FallbackAttemptExceptionAdjuster {
    fun describe(): String
    fun adjust(item: FallbackAttemptException, value: Double): Double
}

data class LabelFallbackAttemptExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackAttemptException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FallbackAttemptExceptionExpiresAtOffsetAdjuster(
    var weight: Double,
) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: FallbackAttemptException, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class MinimumFallbackAttemptExceptionAdjuster(var minimum: Double) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackAttemptException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedFallbackAttemptExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackAttemptException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackAttemptExceptionAdjuster(var percent: Double) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackAttemptException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackAttemptExceptionAdjuster(var cap: Double) : FallbackAttemptExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackAttemptException, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback attempt exception steps in order and keeps a trace of each. */
data class FallbackAttemptExceptionEngine(
    var steps: MutableList<FallbackAttemptExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackAttemptException): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackAttemptExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackAttemptException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackAttemptExceptionEngine(flags: Registry): FallbackAttemptExceptionEngine {
    return FallbackAttemptExceptionEngine(
        steps = mutableListOf<FallbackAttemptExceptionAdjuster>(
            LabelFallbackAttemptExceptionAdjuster(mutableMapOf<String, Double>("online" to 1.24)),
            FallbackAttemptExceptionExpiresAtOffsetAdjuster(1.31),
            MinimumFallbackAttemptExceptionAdjuster(45.0),
            FlaggedFallbackAttemptExceptionAdjuster(flags, "fallback-attempt-exception-global", 3.7),
            PercentFallbackAttemptExceptionAdjuster(-13.0),
            CapFallbackAttemptExceptionAdjuster(33347.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 96639.6,
    )
}
