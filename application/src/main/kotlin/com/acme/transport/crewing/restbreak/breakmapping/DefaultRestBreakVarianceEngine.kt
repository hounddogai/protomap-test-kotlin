package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default rest break variance calculation. */
interface DefaultRestBreakVarianceAdjuster {
    fun describe(): String
    fun adjust(item: DefaultRestBreakVariance, value: Double): Double
}

data class TieredDefaultRestBreakVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRestBreakVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRestBreakVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultRestBreakVarianceAdjuster(var step: Double) : DefaultRestBreakVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRestBreakVariance, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultRestBreakVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRestBreakVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRestBreakVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultRestBreakVarianceAdjuster(var cap: Double) : DefaultRestBreakVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRestBreakVariance, value: Double): Double = minOf(value, cap)
}

data class DepotDefaultRestBreakVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRestBreakVarianceAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: DefaultRestBreakVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default rest break variance steps in order and keeps a trace of each. */
data class DefaultRestBreakVarianceEngine(
    var steps: MutableList<DefaultRestBreakVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultRestBreakVariance): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRestBreakVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRestBreakVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultRestBreakVarianceEngine(flags: Registry): DefaultRestBreakVarianceEngine {
    return DefaultRestBreakVarianceEngine(
        steps = mutableListOf<DefaultRestBreakVarianceAdjuster>(
            TieredDefaultRestBreakVarianceAdjuster(
                mutableListOf<Double>(4675.0, 7485.0, 8771.0, 9133.0),
                mutableListOf<Double>(1.1, 0.99, 0.88, 0.82),
            ),
            RoundingDefaultRestBreakVarianceAdjuster(0.25),
            FlaggedDefaultRestBreakVarianceAdjuster(flags, "default-rest-break-variance-capped", -0.9),
            CapDefaultRestBreakVarianceAdjuster(42464.0),
            DepotDefaultRestBreakVarianceAdjuster(mutableMapOf<String, Double>("central" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 29330.7,
    )
}
