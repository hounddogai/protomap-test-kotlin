package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the attempt exception calculation. */
interface AttemptExceptionFactor {
    fun describe(): String
    fun adjust(item: AttemptException, value: Double): Double
}

data class RoundingAttemptExceptionAdjuster(var step: Double) : AttemptExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptException, value: Double): Double = roundTo(value, step)
}

data class CapAttemptExceptionAdjuster(var cap: Double) : AttemptExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptException, value: Double): Double = minOf(value, cap)
}

data class TieredAttemptExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AttemptExceptionAttemptsOffsetAdjuster(var weight: Double) : AttemptExceptionFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AttemptException, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedAttemptExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt exception steps in order and keeps a trace of each. */
data class AttemptExceptionEngine(
    var steps: MutableList<AttemptExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptException): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptExceptionEngine(flags: Registry): AttemptExceptionEngine {
    return AttemptExceptionEngine(
        steps = mutableListOf<AttemptExceptionFactor>(
            RoundingAttemptExceptionAdjuster(0.5),
            CapAttemptExceptionAdjuster(1751.0),
            TieredAttemptExceptionAdjuster(
                mutableListOf<Double>(1767.0, 7696.0, 7784.0),
                mutableListOf<Double>(1.07, 1.07, 0.93),
            ),
            AttemptExceptionAttemptsOffsetAdjuster(1.59),
            FlaggedAttemptExceptionAdjuster(flags, "attempt-exception-seasonal", -5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 12573.1,
    )
}
