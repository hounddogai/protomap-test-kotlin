package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.numbers.roundTo

/** One step of the default dwell exception calculation. */
interface DefaultDwellExceptionAdjuster {
    fun describe(): String
    fun adjust(item: DefaultDwellException, value: Double): Double
}

data class PercentDefaultDwellExceptionAdjuster(var percent: Double) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultDwellException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultDwellExceptionAdjuster(var minimum: Double) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultDwellException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultDwellExceptionDistanceKmOffsetAdjuster(var weight: Double) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultDwellException, value: Double): Double = value + item.distanceKm * weight
}

data class RoundingDefaultDwellExceptionAdjuster(var step: Double) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultDwellException, value: Double): Double = roundTo(value, step)
}

data class TitleDefaultDwellExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: DefaultDwellException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultDwellExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultDwellExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultDwellException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default dwell exception steps in order and keeps a trace of each. */
data class DefaultDwellExceptionEngine(
    var steps: MutableList<DefaultDwellExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultDwellException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultDwellExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultDwellException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultDwellExceptionEngine(): DefaultDwellExceptionEngine {
    return DefaultDwellExceptionEngine(
        steps = mutableListOf<DefaultDwellExceptionAdjuster>(
            PercentDefaultDwellExceptionAdjuster(3.0),
            MinimumDefaultDwellExceptionAdjuster(33.6),
            DefaultDwellExceptionDistanceKmOffsetAdjuster(0.9),
            RoundingDefaultDwellExceptionAdjuster(5.0),
            TitleDefaultDwellExceptionAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "central" to 0.83, "online" to 0.71),
            ),
            TieredDefaultDwellExceptionAdjuster(
                mutableListOf<Double>(4058.0, 7062.0, 7168.0, 9164.0),
                mutableListOf<Double>(0.91, 1.0, 0.94, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 75317.5,
    )
}
