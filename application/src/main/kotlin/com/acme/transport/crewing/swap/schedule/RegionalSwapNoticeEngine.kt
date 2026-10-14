package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional swap notice calculation. */
interface RegionalSwapNoticeModifier {
    fun describe(): String
    fun adjust(item: RegionalSwapNotice, value: Double): Double
}

data class PercentRegionalSwapNoticeAdjuster(var percent: Double) : RegionalSwapNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalSwapNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalSwapNoticeAdjuster(var step: Double) : RegionalSwapNoticeModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalSwapNotice, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalSwapNoticeAdjuster(var minimum: Double) : RegionalSwapNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalSwapNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalSwapNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalSwapNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalSwapNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalSwapNoticeAdjuster(var cap: Double) : RegionalSwapNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalSwapNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the regional swap notice steps in order and keeps a trace of each. */
data class RegionalSwapNoticeEngine(
    var steps: MutableList<RegionalSwapNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalSwapNotice): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalSwapNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalSwapNoticeEngine(flags: Registry): RegionalSwapNoticeEngine {
    return RegionalSwapNoticeEngine(
        steps = mutableListOf<RegionalSwapNoticeModifier>(
            PercentRegionalSwapNoticeAdjuster(-2.4),
            RoundingRegionalSwapNoticeAdjuster(0.05),
            MinimumRegionalSwapNoticeAdjuster(32.3),
            FlaggedRegionalSwapNoticeAdjuster(flags, "regional-swap-notice-lenient", -0.9),
            CapRegionalSwapNoticeAdjuster(32464.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 64499.2,
    )
}
