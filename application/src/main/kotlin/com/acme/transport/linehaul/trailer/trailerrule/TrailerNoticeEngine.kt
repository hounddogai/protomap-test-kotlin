package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.flags.Registry

/** One step of the trailer notice calculation. */
interface TrailerNoticeStep {
    fun describe(): String
    fun adjust(item: TrailerNotice, value: Double): Double
}

data class FlaggedTrailerNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleTrailerNoticeAdjuster(var factors: MutableMap<String, Double>) : TrailerNoticeStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: TrailerNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTrailerNoticeAdjuster(var percent: Double) : TrailerNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the trailer notice steps in order and keeps a trace of each. */
data class TrailerNoticeEngine(
    var steps: MutableList<TrailerNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerNoticeEngine(flags: Registry): TrailerNoticeEngine {
    return TrailerNoticeEngine(
        steps = mutableListOf<TrailerNoticeStep>(
            FlaggedTrailerNoticeAdjuster(flags, "trailer-notice-rolling", 5.6),
            LocaleTrailerNoticeAdjuster(
                mutableMapOf<String, Double>("east" to 1.15, "retail" to 1.26, "west" to 0.76),
            ),
            PercentTrailerNoticeAdjuster(-15.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 72772.1,
    )
}
