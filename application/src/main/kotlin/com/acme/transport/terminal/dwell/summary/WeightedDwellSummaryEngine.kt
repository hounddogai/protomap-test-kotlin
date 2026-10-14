package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.flags.Registry

/** One step of the weighted dwell summary calculation. */
interface WeightedDwellSummaryFactor {
    fun describe(): String
    fun adjust(item: WeightedDwellSummary, value: Double): Double
}

data class WeightedDwellSummaryPalletsOffsetAdjuster(var weight: Double) : WeightedDwellSummaryFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: WeightedDwellSummary, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class LabelWeightedDwellSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDwellSummaryFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedDwellSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedDwellSummaryAdjuster(var percent: Double) : WeightedDwellSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDwellSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedWeightedDwellSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDwellSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDwellSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted dwell summary steps in order and keeps a trace of each. */
data class WeightedDwellSummaryEngine(
    var steps: MutableList<WeightedDwellSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedDwellSummary): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDwellSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDwellSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedDwellSummaryEngine(flags: Registry): WeightedDwellSummaryEngine {
    return WeightedDwellSummaryEngine(
        steps = mutableListOf<WeightedDwellSummaryFactor>(
            WeightedDwellSummaryPalletsOffsetAdjuster(0.06),
            LabelWeightedDwellSummaryAdjuster(mutableMapOf<String, Double>("retail" to 0.82, "west" to 1.38)),
            PercentWeightedDwellSummaryAdjuster(14.9),
            FlaggedWeightedDwellSummaryAdjuster(flags, "weighted-dwell-summary-rolling", 8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 6433.4,
    )
}
