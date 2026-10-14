package com.acme.transport.customs.hold.forecast

/** One step of the primary hold notice calculation. */
interface PrimaryHoldNoticeFactor {
    fun describe(): String
    fun adjust(item: PrimaryHoldNotice, value: Double): Double
}

data class SourcePrimaryHoldNoticeAdjuster(var factors: MutableMap<String, Double>) : PrimaryHoldNoticeFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: PrimaryHoldNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryHoldNoticeAdjuster(var percent: Double) : PrimaryHoldNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryHoldNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPrimaryHoldNoticeAdjuster(var minimum: Double) : PrimaryHoldNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryHoldNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary hold notice steps in order and keeps a trace of each. */
data class PrimaryHoldNoticeEngine(
    var steps: MutableList<PrimaryHoldNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryHoldNotice): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryHoldNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryHoldNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryHoldNoticeEngine(): PrimaryHoldNoticeEngine {
    return PrimaryHoldNoticeEngine(
        steps = mutableListOf<PrimaryHoldNoticeFactor>(
            SourcePrimaryHoldNoticeAdjuster(
                mutableMapOf<String, Double>("central" to 1.33, "south" to 0.73, "retail" to 1.23),
            ),
            PercentPrimaryHoldNoticeAdjuster(-9.6),
            MinimumPrimaryHoldNoticeAdjuster(13.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 95994.1,
    )
}
