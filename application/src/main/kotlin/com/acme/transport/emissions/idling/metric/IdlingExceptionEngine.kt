package com.acme.transport.emissions.idling.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling exception calculation. */
interface IdlingExceptionAdjuster {
    fun describe(): String
    fun adjust(item: IdlingException, value: Double): Double
}

data class TieredIdlingExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class IdlingExceptionDurationMinutesOffsetAdjuster(var weight: Double) : IdlingExceptionAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: IdlingException, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapIdlingExceptionAdjuster(var cap: Double) : IdlingExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingException, value: Double): Double = minOf(value, cap)
}

data class MinimumIdlingExceptionAdjuster(var minimum: Double) : IdlingExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the idling exception steps in order and keeps a trace of each. */
data class IdlingExceptionEngine(
    var steps: MutableList<IdlingExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingException): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingExceptionEngine(): IdlingExceptionEngine {
    return IdlingExceptionEngine(
        steps = mutableListOf<IdlingExceptionAdjuster>(
            TieredIdlingExceptionAdjuster(
                mutableListOf<Double>(1215.0, 3489.0),
                mutableListOf<Double>(1.16, 1.12),
            ),
            IdlingExceptionDurationMinutesOffsetAdjuster(1.55),
            CapIdlingExceptionAdjuster(21770.0),
            MinimumIdlingExceptionAdjuster(17.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 24008.1,
    )
}
