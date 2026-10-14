package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped coaching notice calculation. */
interface CappedCoachingNoticeAdjuster {
    fun describe(): String
    fun adjust(item: CappedCoachingNotice, value: Double): Double
}

data class TieredCappedCoachingNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCoachingNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCoachingNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedCoachingNoticeAdjuster(var cap: Double) : CappedCoachingNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCoachingNotice, value: Double): Double = minOf(value, cap)
}

data class PercentCappedCoachingNoticeAdjuster(var percent: Double) : CappedCoachingNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCoachingNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedCoachingNoticeAdjuster(var step: Double) : CappedCoachingNoticeAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedCoachingNotice, value: Double): Double = roundTo(value, step)
}

/** Runs the capped coaching notice steps in order and keeps a trace of each. */
data class CappedCoachingNoticeEngine(
    var steps: MutableList<CappedCoachingNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedCoachingNotice): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedCoachingNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedCoachingNoticeEngine(): CappedCoachingNoticeEngine {
    return CappedCoachingNoticeEngine(
        steps = mutableListOf<CappedCoachingNoticeAdjuster>(
            TieredCappedCoachingNoticeAdjuster(
                mutableListOf<Double>(261.0, 7434.0),
                mutableListOf<Double>(0.82, 0.98),
            ),
            CapCappedCoachingNoticeAdjuster(6375.0),
            PercentCappedCoachingNoticeAdjuster(15.6),
            RoundingCappedCoachingNoticeAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 6439.7,
    )
}
