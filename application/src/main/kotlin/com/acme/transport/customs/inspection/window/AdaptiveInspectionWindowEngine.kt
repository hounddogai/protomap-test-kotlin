package com.acme.transport.customs.inspection.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive inspection window calculation. */
interface AdaptiveInspectionWindowAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionWindow, value: Double): Double
}

data class DepotAdaptiveInspectionWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAdaptiveInspectionWindowAdjuster(var step: Double) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveInspectionWindowAdjuster(var percent: Double) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveInspectionWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveInspectionWindowEtaMinutesOffsetAdjuster(
    var weight: Double,
) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class MinimumAdaptiveInspectionWindowAdjuster(var minimum: Double) : AdaptiveInspectionWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive inspection window steps in order and keeps a trace of each. */
data class AdaptiveInspectionWindowEngine(
    var steps: MutableList<AdaptiveInspectionWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveInspectionWindow): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionWindowEngine(): AdaptiveInspectionWindowEngine {
    return AdaptiveInspectionWindowEngine(
        steps = mutableListOf<AdaptiveInspectionWindowAdjuster>(
            DepotAdaptiveInspectionWindowAdjuster(
                mutableMapOf<String, Double>("online" to 1.25, "north" to 1.3, "east" to 1.34),
            ),
            RoundingAdaptiveInspectionWindowAdjuster(0.1),
            PercentAdaptiveInspectionWindowAdjuster(-18.0),
            TieredAdaptiveInspectionWindowAdjuster(
                mutableListOf<Double>(5264.0, 6001.0),
                mutableListOf<Double>(0.92, 0.95),
            ),
            AdaptiveInspectionWindowEtaMinutesOffsetAdjuster(1.06),
            MinimumAdaptiveInspectionWindowAdjuster(5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 3084.9,
    )
}
