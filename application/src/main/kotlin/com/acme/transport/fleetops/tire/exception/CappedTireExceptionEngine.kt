package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped tire exception calculation. */
interface CappedTireExceptionAdjuster {
    fun describe(): String
    fun adjust(item: CappedTireException, value: Double): Double
}

data class MinimumCappedTireExceptionAdjuster(var minimum: Double) : CappedTireExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTireException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedTireExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTireExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTireException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedTireExceptionAdjuster(var step: Double) : CappedTireExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTireException, value: Double): Double = roundTo(value, step)
}

data class PercentCappedTireExceptionAdjuster(var percent: Double) : CappedTireExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTireException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedTireExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTireExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTireException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped tire exception steps in order and keeps a trace of each. */
data class CappedTireExceptionEngine(
    var steps: MutableList<CappedTireExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedTireException): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedTireExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedTireException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedTireExceptionEngine(flags: Registry): CappedTireExceptionEngine {
    return CappedTireExceptionEngine(
        steps = mutableListOf<CappedTireExceptionAdjuster>(
            MinimumCappedTireExceptionAdjuster(6.7),
            TieredCappedTireExceptionAdjuster(
                mutableListOf<Double>(4912.0, 6096.0, 9058.0),
                mutableListOf<Double>(0.89, 0.83, 0.88),
            ),
            RoundingCappedTireExceptionAdjuster(0.05),
            PercentCappedTireExceptionAdjuster(-10.6),
            FlaggedCappedTireExceptionAdjuster(flags, "capped-tire-exception-capped", 9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 89543.5,
    )
}
