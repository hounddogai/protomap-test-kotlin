package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered sortation summary calculation. */
interface TieredSortationSummaryAdjuster {
    fun describe(): String
    fun adjust(item: TieredSortationSummary, value: Double): Double
}

data class TieredTieredSortationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredSortationSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredSortationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredSortationSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredSortationSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredSortationSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelTieredSortationSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredSortationSummaryAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredSortationSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTieredSortationSummaryAdjuster(var step: Double) : TieredSortationSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredSortationSummary, value: Double): Double = roundTo(value, step)
}

data class CapTieredSortationSummaryAdjuster(var cap: Double) : TieredSortationSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredSortationSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered sortation summary steps in order and keeps a trace of each. */
data class TieredSortationSummaryEngine(
    var steps: MutableList<TieredSortationSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredSortationSummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredSortationSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredSortationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredSortationSummaryEngine(flags: Registry): TieredSortationSummaryEngine {
    return TieredSortationSummaryEngine(
        steps = mutableListOf<TieredSortationSummaryAdjuster>(
            TieredTieredSortationSummaryAdjuster(
                mutableListOf<Double>(1774.0, 3874.0, 4288.0, 5972.0),
                mutableListOf<Double>(0.96, 0.84, 0.98, 1.1),
            ),
            FlaggedTieredSortationSummaryAdjuster(flags, "tiered-sortation-summary-scheduled", -2.6),
            LabelTieredSortationSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.2)),
            RoundingTieredSortationSummaryAdjuster(0.25),
            CapTieredSortationSummaryAdjuster(10004.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 1554.2,
    )
}
