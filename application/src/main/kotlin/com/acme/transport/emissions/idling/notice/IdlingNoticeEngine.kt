package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling notice calculation. */
interface IdlingNoticeFactor {
    fun describe(): String
    fun adjust(item: IdlingNotice, value: Double): Double
}

data class ChannelIdlingNoticeAdjuster(var factors: MutableMap<String, Double>) : IdlingNoticeFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: IdlingNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class IdlingNoticeStartsAtOffsetAdjuster(var weight: Double) : IdlingNoticeFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: IdlingNotice, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class FlaggedIdlingNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the idling notice steps in order and keeps a trace of each. */
data class IdlingNoticeEngine(
    var steps: MutableList<IdlingNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingNotice): Double {
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

    fun evaluateAll(items: MutableList<IdlingNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingNoticeEngine(flags: Registry): IdlingNoticeEngine {
    return IdlingNoticeEngine(
        steps = mutableListOf<IdlingNoticeFactor>(
            ChannelIdlingNoticeAdjuster(mutableMapOf<String, Double>("east" to 1.12)),
            IdlingNoticeStartsAtOffsetAdjuster(1.33),
            FlaggedIdlingNoticeAdjuster(flags, "idling-notice-manual", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 72595.8,
    )
}
