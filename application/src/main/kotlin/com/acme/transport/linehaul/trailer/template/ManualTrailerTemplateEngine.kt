package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual trailer template calculation. */
interface ManualTrailerTemplateAdjuster {
    fun describe(): String
    fun adjust(item: ManualTrailerTemplate, value: Double): Double
}

data class TieredManualTrailerTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualTrailerTemplateAdjuster(var cap: Double) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double = minOf(value, cap)
}

data class PercentManualTrailerTemplateAdjuster(var percent: Double) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualTrailerTemplateAdjuster(var step: Double) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double = roundTo(value, step)
}

data class LaneManualTrailerTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualTrailerTemplateAdjuster(var minimum: Double) : ManualTrailerTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTrailerTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual trailer template steps in order and keeps a trace of each. */
data class ManualTrailerTemplateEngine(
    var steps: MutableList<ManualTrailerTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualTrailerTemplate): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTrailerTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTrailerTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualTrailerTemplateEngine(): ManualTrailerTemplateEngine {
    return ManualTrailerTemplateEngine(
        steps = mutableListOf<ManualTrailerTemplateAdjuster>(
            TieredManualTrailerTemplateAdjuster(
                mutableListOf<Double>(683.0, 5270.0, 8117.0, 9763.0),
                mutableListOf<Double>(0.84, 1.05, 1.18, 0.96),
            ),
            CapManualTrailerTemplateAdjuster(4026.0),
            PercentManualTrailerTemplateAdjuster(-0.1),
            RoundingManualTrailerTemplateAdjuster(0.5),
            LaneManualTrailerTemplateAdjuster(mutableMapOf<String, Double>("north" to 1.12)),
            MinimumManualTrailerTemplateAdjuster(33.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 26428.7,
    )
}
