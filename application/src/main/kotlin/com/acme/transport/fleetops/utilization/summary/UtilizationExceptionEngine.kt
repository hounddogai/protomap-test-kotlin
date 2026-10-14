package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization exception calculation. */
interface UtilizationExceptionFactor {
    fun describe(): String
    fun adjust(item: UtilizationException, value: Double): Double
}

data class RoundingUtilizationExceptionAdjuster(var step: Double) : UtilizationExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationException, value: Double): Double = roundTo(value, step)
}

data class UtilizationExceptionPriorityOffsetAdjuster(var weight: Double) : UtilizationExceptionFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: UtilizationException, value: Double): Double = value + item.priority.toDouble() * weight
}

data class SourceUtilizationExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationExceptionFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: UtilizationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedUtilizationExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredUtilizationExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization exception steps in order and keeps a trace of each. */
data class UtilizationExceptionEngine(
    var steps: MutableList<UtilizationExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationException): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationExceptionFactor = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationExceptionEngine(flags: Registry): UtilizationExceptionEngine {
    return UtilizationExceptionEngine(
        steps = mutableListOf<UtilizationExceptionFactor>(
            RoundingUtilizationExceptionAdjuster(5.0),
            UtilizationExceptionPriorityOffsetAdjuster(1.53),
            SourceUtilizationExceptionAdjuster(
                mutableMapOf<String, Double>("online" to 1.26, "south" to 1.03, "central" to 1.24),
            ),
            FlaggedUtilizationExceptionAdjuster(flags, "utilization-exception-tiered", -5.9),
            TieredUtilizationExceptionAdjuster(
                mutableListOf<Double>(526.0, 690.0, 8270.0),
                mutableListOf<Double>(1.09, 0.88, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 34875.9,
    )
}
