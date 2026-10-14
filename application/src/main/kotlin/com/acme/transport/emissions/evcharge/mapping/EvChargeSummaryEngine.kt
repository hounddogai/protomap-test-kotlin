package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the ev charge summary calculation. */
interface EvChargeSummaryFactor {
    fun describe(): String
    fun adjust(item: EvChargeSummary, value: Double): Double
}

data class EvChargeSummaryScoreOffsetAdjuster(var weight: Double) : EvChargeSummaryFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: EvChargeSummary, value: Double): Double = value + item.score * weight
}

data class FlaggedEvChargeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumEvChargeSummaryAdjuster(var minimum: Double) : EvChargeSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapEvChargeSummaryAdjuster(var cap: Double) : EvChargeSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeSummary, value: Double): Double = minOf(value, cap)
}

data class PercentEvChargeSummaryAdjuster(var percent: Double) : EvChargeSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: EvChargeSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the ev charge summary steps in order and keeps a trace of each. */
data class EvChargeSummaryEngine(
    var steps: MutableList<EvChargeSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EvChargeSummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEvChargeSummaryEngine(flags: Registry): EvChargeSummaryEngine {
    return EvChargeSummaryEngine(
        steps = mutableListOf<EvChargeSummaryFactor>(
            EvChargeSummaryScoreOffsetAdjuster(0.13),
            FlaggedEvChargeSummaryAdjuster(flags, "ev-charge-summary-adaptive", 2.9),
            MinimumEvChargeSummaryAdjuster(8.8),
            CapEvChargeSummaryAdjuster(45531.0),
            PercentEvChargeSummaryAdjuster(-4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 39346.4,
    )
}
