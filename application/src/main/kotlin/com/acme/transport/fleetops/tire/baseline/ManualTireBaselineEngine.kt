package com.acme.transport.fleetops.tire.baseline

/** One step of the manual tire baseline calculation. */
interface ManualTireBaselineStep {
    fun describe(): String
    fun adjust(item: ManualTireBaseline, value: Double): Double
}

data class CapManualTireBaselineAdjuster(var cap: Double) : ManualTireBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTireBaseline, value: Double): Double = minOf(value, cap)
}

data class MinimumManualTireBaselineAdjuster(var minimum: Double) : ManualTireBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTireBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleManualTireBaselineAdjuster(var factors: MutableMap<String, Double>) : ManualTireBaselineStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualTireBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualTireBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTireBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTireBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual tire baseline steps in order and keeps a trace of each. */
data class ManualTireBaselineEngine(
    var steps: MutableList<ManualTireBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTireBaseline): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTireBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTireBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTireBaselineEngine(): ManualTireBaselineEngine {
    return ManualTireBaselineEngine(
        steps = mutableListOf<ManualTireBaselineStep>(
            CapManualTireBaselineAdjuster(44796.0),
            MinimumManualTireBaselineAdjuster(13.1),
            TitleManualTireBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 0.81, "central" to 1.07, "north" to 1.07),
            ),
            TieredManualTireBaselineAdjuster(
                mutableListOf<Double>(683.0, 2599.0, 2985.0, 6889.0),
                mutableListOf<Double>(1.16, 1.19, 1.1, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 56492.6,
    )
}
