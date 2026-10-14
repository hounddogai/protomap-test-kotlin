package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive cross dock summary calculation. */
interface AdaptiveCrossDockSummaryFactor {
    fun describe(): String
    fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double
}

data class MinimumAdaptiveCrossDockSummaryAdjuster(var minimum: Double) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveCrossDockSummaryAdjuster(var percent: Double) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveCrossDockSummaryAdjuster(var cap: Double) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveCrossDockSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveCrossDockSummaryFloorValueOffsetAdjuster(
    var weight: Double,
) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double = value + item.floorValue * weight
}

data class FlaggedAdaptiveCrossDockSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCrossDockSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCrossDockSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive cross dock summary steps in order and keeps a trace of each. */
data class AdaptiveCrossDockSummaryEngine(
    var steps: MutableList<AdaptiveCrossDockSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveCrossDockSummary): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCrossDockSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCrossDockSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveCrossDockSummaryEngine(flags: Registry): AdaptiveCrossDockSummaryEngine {
    return AdaptiveCrossDockSummaryEngine(
        steps = mutableListOf<AdaptiveCrossDockSummaryFactor>(
            MinimumAdaptiveCrossDockSummaryAdjuster(16.6),
            PercentAdaptiveCrossDockSummaryAdjuster(-14.2),
            CapAdaptiveCrossDockSummaryAdjuster(2456.0),
            TieredAdaptiveCrossDockSummaryAdjuster(
                mutableListOf<Double>(3334.0, 5005.0, 6720.0),
                mutableListOf<Double>(1.08, 1.12, 0.85),
            ),
            AdaptiveCrossDockSummaryFloorValueOffsetAdjuster(1.59),
            FlaggedAdaptiveCrossDockSummaryAdjuster(flags, "adaptive-cross-dock-summary-manual", 8.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 28934.6,
    )
}
