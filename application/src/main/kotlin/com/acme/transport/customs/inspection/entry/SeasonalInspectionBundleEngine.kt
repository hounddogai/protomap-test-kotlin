package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal inspection bundle calculation. */
interface SeasonalInspectionBundleAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalInspectionBundle, value: Double): Double
}

data class NameSeasonalInspectionBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalInspectionBundleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SeasonalInspectionBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalInspectionBundleAdjuster(var percent: Double) : SeasonalInspectionBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalInspectionBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalInspectionBundleAdjuster(var step: Double) : SeasonalInspectionBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalInspectionBundle, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalInspectionBundleAdjuster(var cap: Double) : SeasonalInspectionBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalInspectionBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumSeasonalInspectionBundleAdjuster(var minimum: Double) : SeasonalInspectionBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalInspectionBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal inspection bundle steps in order and keeps a trace of each. */
data class SeasonalInspectionBundleEngine(
    var steps: MutableList<SeasonalInspectionBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalInspectionBundle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalInspectionBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalInspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalInspectionBundleEngine(): SeasonalInspectionBundleEngine {
    return SeasonalInspectionBundleEngine(
        steps = mutableListOf<SeasonalInspectionBundleAdjuster>(
            NameSeasonalInspectionBundleAdjuster(
                mutableMapOf<String, Double>("south" to 1.2, "north" to 0.85, "online" to 1.03),
            ),
            PercentSeasonalInspectionBundleAdjuster(14.1),
            RoundingSeasonalInspectionBundleAdjuster(1.0),
            CapSeasonalInspectionBundleAdjuster(28083.0),
            MinimumSeasonalInspectionBundleAdjuster(30.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 62666.2,
    )
}
