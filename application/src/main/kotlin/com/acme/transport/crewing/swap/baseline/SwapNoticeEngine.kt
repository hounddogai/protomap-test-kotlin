package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the swap notice calculation. */
interface SwapNoticeModifier {
    fun describe(): String
    fun adjust(item: SwapNotice, value: Double): Double
}

data class PercentSwapNoticeAdjuster(var percent: Double) : SwapNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleSwapNoticeAdjuster(var factors: MutableMap<String, Double>) : SwapNoticeModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: SwapNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSwapNoticeAdjuster(var cap: Double) : SwapNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedSwapNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapNoticeModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SwapNoticeBatchSizeOffsetAdjuster(var weight: Double) : SwapNoticeModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: SwapNotice, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class MinimumSwapNoticeAdjuster(var minimum: Double) : SwapNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the swap notice steps in order and keeps a trace of each. */
data class SwapNoticeEngine(
    var steps: MutableList<SwapNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapNoticeModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SwapNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapNoticeEngine(flags: Registry): SwapNoticeEngine {
    return SwapNoticeEngine(
        steps = mutableListOf<SwapNoticeModifier>(
            PercentSwapNoticeAdjuster(-15.8),
            TitleSwapNoticeAdjuster(mutableMapOf<String, Double>("central" to 0.77)),
            CapSwapNoticeAdjuster(23014.0),
            FlaggedSwapNoticeAdjuster(flags, "swap-notice-pending", -5.9),
            SwapNoticeBatchSizeOffsetAdjuster(1.3),
            MinimumSwapNoticeAdjuster(30.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 87527.2,
    )
}
