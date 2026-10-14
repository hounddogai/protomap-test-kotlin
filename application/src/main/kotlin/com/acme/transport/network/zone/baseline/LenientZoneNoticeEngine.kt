package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient zone notice calculation. */
interface LenientZoneNoticeFactor {
    fun describe(): String
    fun adjust(item: LenientZoneNotice, value: Double): Double
}

data class MinimumLenientZoneNoticeAdjuster(var minimum: Double) : LenientZoneNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientZoneNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientZoneNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientZoneNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientZoneNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLenientZoneNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientZoneNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientZoneNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientZoneNoticeAdjuster(var percent: Double) : LenientZoneNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientZoneNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient zone notice steps in order and keeps a trace of each. */
data class LenientZoneNoticeEngine(
    var steps: MutableList<LenientZoneNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientZoneNotice): Double {
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

    fun evaluateAll(items: MutableList<LenientZoneNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientZoneNoticeEngine(flags: Registry): LenientZoneNoticeEngine {
    return LenientZoneNoticeEngine(
        steps = mutableListOf<LenientZoneNoticeFactor>(
            MinimumLenientZoneNoticeAdjuster(46.1),
            TieredLenientZoneNoticeAdjuster(
                mutableListOf<Double>(3421.0, 5934.0),
                mutableListOf<Double>(0.93, 0.86),
            ),
            FlaggedLenientZoneNoticeAdjuster(flags, "lenient-zone-notice-capped", 6.8),
            PercentLenientZoneNoticeAdjuster(-0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 37358.6,
    )
}
