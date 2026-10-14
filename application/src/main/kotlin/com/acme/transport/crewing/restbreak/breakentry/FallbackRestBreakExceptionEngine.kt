package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback rest break exception calculation. */
interface FallbackRestBreakExceptionModifier {
    fun describe(): String
    fun adjust(item: FallbackRestBreakException, value: Double): Double
}

data class TieredFallbackRestBreakExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackRestBreakExceptionAdjuster(var step: Double) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double = roundTo(value, step)
}

data class MinimumFallbackRestBreakExceptionAdjuster(var minimum: Double) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackRestBreakExceptionAdjuster(var percent: Double) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackRestBreakExceptionAdjuster(var cap: Double) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackRestBreakExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRestBreakExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRestBreakException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback rest break exception steps in order and keeps a trace of each. */
data class FallbackRestBreakExceptionEngine(
    var steps: MutableList<FallbackRestBreakExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRestBreakException): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackRestBreakExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackRestBreakException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRestBreakExceptionEngine(flags: Registry): FallbackRestBreakExceptionEngine {
    return FallbackRestBreakExceptionEngine(
        steps = mutableListOf<FallbackRestBreakExceptionModifier>(
            TieredFallbackRestBreakExceptionAdjuster(
                mutableListOf<Double>(365.0, 4777.0),
                mutableListOf<Double>(0.96, 1.12),
            ),
            RoundingFallbackRestBreakExceptionAdjuster(0.05),
            MinimumFallbackRestBreakExceptionAdjuster(6.1),
            PercentFallbackRestBreakExceptionAdjuster(8.8),
            CapFallbackRestBreakExceptionAdjuster(27788.0),
            FlaggedFallbackRestBreakExceptionAdjuster(flags, "fallback-rest-break-exception-regional", 5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 95332.4,
    )
}
