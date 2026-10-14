package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped attempt exception calculation. */
interface CappedAttemptExceptionAdjuster {
    fun describe(): String
    fun adjust(item: CappedAttemptException, value: Double): Double
}

data class FlaggedCappedAttemptExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAttemptExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAttemptException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedAttemptExceptionRetriesOffsetAdjuster(var weight: Double) : CappedAttemptExceptionAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: CappedAttemptException, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class CapCappedAttemptExceptionAdjuster(var cap: Double) : CappedAttemptExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedAttemptException, value: Double): Double = minOf(value, cap)
}

/** Runs the capped attempt exception steps in order and keeps a trace of each. */
data class CappedAttemptExceptionEngine(
    var steps: MutableList<CappedAttemptExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedAttemptException): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedAttemptException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedAttemptExceptionEngine(flags: Registry): CappedAttemptExceptionEngine {
    return CappedAttemptExceptionEngine(
        steps = mutableListOf<CappedAttemptExceptionAdjuster>(
            FlaggedCappedAttemptExceptionAdjuster(flags, "capped-attempt-exception-manual", -5.7),
            CappedAttemptExceptionRetriesOffsetAdjuster(1.42),
            CapCappedAttemptExceptionAdjuster(35736.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 79596.7,
    )
}
