package com.acme.transport.terminal.dwell.group

/** One step of the adaptive dwell group calculation. */
interface AdaptiveDwellGroupStep {
    fun describe(): String
    fun adjust(item: AdaptiveDwellGroup, value: Double): Double
}

data class CapAdaptiveDwellGroupAdjuster(var cap: Double) : AdaptiveDwellGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveDwellGroup, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveDwellGroupAdjuster(var percent: Double) : AdaptiveDwellGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDwellGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveDwellGroupGraceDaysOffsetAdjuster(var weight: Double) : AdaptiveDwellGroupStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: AdaptiveDwellGroup, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class TieredAdaptiveDwellGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDwellGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDwellGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveDwellGroupAdjuster(var minimum: Double) : AdaptiveDwellGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDwellGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive dwell group steps in order and keeps a trace of each. */
data class AdaptiveDwellGroupEngine(
    var steps: MutableList<AdaptiveDwellGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveDwellGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDwellGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDwellGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveDwellGroupEngine(): AdaptiveDwellGroupEngine {
    return AdaptiveDwellGroupEngine(
        steps = mutableListOf<AdaptiveDwellGroupStep>(
            CapAdaptiveDwellGroupAdjuster(29356.0),
            PercentAdaptiveDwellGroupAdjuster(-5.2),
            AdaptiveDwellGroupGraceDaysOffsetAdjuster(1.78),
            TieredAdaptiveDwellGroupAdjuster(
                mutableListOf<Double>(1573.0, 2197.0, 6440.0, 7846.0),
                mutableListOf<Double>(1.1, 0.99, 1.11, 1.02),
            ),
            MinimumAdaptiveDwellGroupAdjuster(5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 71619.7,
    )
}
