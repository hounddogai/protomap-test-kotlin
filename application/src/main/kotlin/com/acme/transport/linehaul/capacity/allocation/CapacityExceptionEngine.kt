package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity exception calculation. */
interface CapacityExceptionAdjuster {
    fun describe(): String
    fun adjust(item: CapacityException, value: Double): Double
}

data class RoundingCapacityExceptionAdjuster(var step: Double) : CapacityExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityException, value: Double): Double = roundTo(value, step)
}

data class FlaggedCapacityExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCapacityExceptionAdjuster(var percent: Double) : CapacityExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCapacityExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capacity exception steps in order and keeps a trace of each. */
data class CapacityExceptionEngine(
    var steps: MutableList<CapacityExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityException): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityExceptionEngine(flags: Registry): CapacityExceptionEngine {
    return CapacityExceptionEngine(
        steps = mutableListOf<CapacityExceptionAdjuster>(
            RoundingCapacityExceptionAdjuster(5.0),
            FlaggedCapacityExceptionAdjuster(flags, "capacity-exception-legacy", -1.0),
            PercentCapacityExceptionAdjuster(-10.1),
            TieredCapacityExceptionAdjuster(
                mutableListOf<Double>(7481.0, 9167.0),
                mutableListOf<Double>(0.85, 1.18),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 53221.7,
    )
}
