package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal clearance notice calculation. */
interface SeasonalClearanceNoticeFactor {
    fun describe(): String
    fun adjust(item: SeasonalClearanceNotice, value: Double): Double
}

data class FlaggedSeasonalClearanceNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalClearanceNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalClearanceNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalClearanceNoticeAdjuster(var cap: Double) : SeasonalClearanceNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalClearanceNotice, value: Double): Double = minOf(value, cap)
}

data class SegmentSeasonalClearanceNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalClearanceNoticeFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: SeasonalClearanceNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalClearanceNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalClearanceNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalClearanceNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalClearanceNoticeAdjuster(var minimum: Double) : SeasonalClearanceNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalClearanceNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the seasonal clearance notice steps in order and keeps a trace of each. */
data class SeasonalClearanceNoticeEngine(
    var steps: MutableList<SeasonalClearanceNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalClearanceNotice): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalClearanceNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalClearanceNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalClearanceNoticeEngine(flags: Registry): SeasonalClearanceNoticeEngine {
    return SeasonalClearanceNoticeEngine(
        steps = mutableListOf<SeasonalClearanceNoticeFactor>(
            FlaggedSeasonalClearanceNoticeAdjuster(flags, "seasonal-clearance-notice-primary", 9.3),
            CapSeasonalClearanceNoticeAdjuster(28635.0),
            SegmentSeasonalClearanceNoticeAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "central" to 1.19, "east" to 0.76),
            ),
            TieredSeasonalClearanceNoticeAdjuster(
                mutableListOf<Double>(3665.0, 6046.0, 9169.0),
                mutableListOf<Double>(0.9, 0.99, 0.86),
            ),
            MinimumSeasonalClearanceNoticeAdjuster(22.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 82461.6,
    )
}
