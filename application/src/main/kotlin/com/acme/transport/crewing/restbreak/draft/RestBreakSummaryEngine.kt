package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.flags.Registry

/** One step of the rest break summary calculation. */
interface RestBreakSummaryStep {
    fun describe(): String
    fun adjust(item: RestBreakSummary, value: Double): Double
}

data class PercentRestBreakSummaryAdjuster(var percent: Double) : RestBreakSummaryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRestBreakSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceRestBreakSummaryAdjuster(var factors: MutableMap<String, Double>) : RestBreakSummaryStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RestBreakSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rest break summary steps in order and keeps a trace of each. */
data class RestBreakSummaryEngine(
    var steps: MutableList<RestBreakSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakSummary): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakSummaryEngine(flags: Registry): RestBreakSummaryEngine {
    return RestBreakSummaryEngine(
        steps = mutableListOf<RestBreakSummaryStep>(
            PercentRestBreakSummaryAdjuster(-18.1),
            FlaggedRestBreakSummaryAdjuster(flags, "rest-break-summary-batched", -9.0),
            SourceRestBreakSummaryAdjuster(mutableMapOf<String, Double>("online" to 0.98, "north" to 0.98)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 20221.2,
    )
}
