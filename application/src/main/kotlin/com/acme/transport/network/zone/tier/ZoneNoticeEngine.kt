package com.acme.transport.network.zone.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the zone notice calculation. */
interface ZoneNoticeFactor {
    fun describe(): String
    fun adjust(item: ZoneNotice, value: Double): Double
}

data class CapZoneNoticeAdjuster(var cap: Double) : ZoneNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneNotice, value: Double): Double = minOf(value, cap)
}

data class MinimumZoneNoticeAdjuster(var minimum: Double) : ZoneNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryZoneNoticeAdjuster(var factors: MutableMap<String, Double>) : ZoneNoticeFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ZoneNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the zone notice steps in order and keeps a trace of each. */
data class ZoneNoticeEngine(
    var steps: MutableList<ZoneNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneNotice): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneNoticeEngine(): ZoneNoticeEngine {
    return ZoneNoticeEngine(
        steps = mutableListOf<ZoneNoticeFactor>(
            CapZoneNoticeAdjuster(45613.0),
            MinimumZoneNoticeAdjuster(4.5),
            CategoryZoneNoticeAdjuster(mutableMapOf<String, Double>("central" to 1.29, "online" to 1.15)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 79159.9,
    )
}
