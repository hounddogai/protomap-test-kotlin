package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial exception calculation. */
interface AccessorialExceptionFactor {
    fun describe(): String
    fun adjust(item: AccessorialException, value: Double): Double
}

data class TieredAccessorialExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AccessorialExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AccessorialException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAccessorialExceptionAdjuster(var cap: Double) : AccessorialExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialException, value: Double): Double = minOf(value, cap)
}

data class FlaggedAccessorialExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAccessorialExceptionAdjuster(var minimum: Double) : AccessorialExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAccessorialExceptionAdjuster(var step: Double) : AccessorialExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialException, value: Double): Double = roundTo(value, step)
}

data class AccessorialExceptionDiscountOffsetAdjuster(var weight: Double) : AccessorialExceptionFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AccessorialException, value: Double): Double = value + item.discount * weight
}

/** Runs the accessorial exception steps in order and keeps a trace of each. */
data class AccessorialExceptionEngine(
    var steps: MutableList<AccessorialExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialException): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialExceptionEngine(flags: Registry): AccessorialExceptionEngine {
    return AccessorialExceptionEngine(
        steps = mutableListOf<AccessorialExceptionFactor>(
            TieredAccessorialExceptionAdjuster(
                mutableListOf<Double>(1372.0, 2773.0, 3512.0, 9770.0),
                mutableListOf<Double>(1.19, 1.08, 0.89, 1.06),
            ),
            CapAccessorialExceptionAdjuster(36302.0),
            FlaggedAccessorialExceptionAdjuster(flags, "accessorial-exception-primary", 2.8),
            MinimumAccessorialExceptionAdjuster(6.4),
            RoundingAccessorialExceptionAdjuster(5.0),
            AccessorialExceptionDiscountOffsetAdjuster(1.54),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 10380.9,
    )
}
