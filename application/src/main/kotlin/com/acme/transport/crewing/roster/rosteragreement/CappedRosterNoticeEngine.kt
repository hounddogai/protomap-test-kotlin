package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped roster notice calculation. */
interface CappedRosterNoticeFactor {
    fun describe(): String
    fun adjust(item: CappedRosterNotice, value: Double): Double
}

data class RoundingCappedRosterNoticeAdjuster(var step: Double) : CappedRosterNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedRosterNotice, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedRosterNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedRosterNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedRosterNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedRosterNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRosterNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRosterNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedRosterNoticeAdjuster(var cap: Double) : CappedRosterNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedRosterNotice, value: Double): Double = minOf(value, cap)
}

data class CappedRosterNoticePalletsOffsetAdjuster(var weight: Double) : CappedRosterNoticeFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: CappedRosterNotice, value: Double): Double = value + item.pallets.toDouble() * weight
}

/** Runs the capped roster notice steps in order and keeps a trace of each. */
data class CappedRosterNoticeEngine(
    var steps: MutableList<CappedRosterNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedRosterNotice): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRosterNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedRosterNoticeEngine(flags: Registry): CappedRosterNoticeEngine {
    return CappedRosterNoticeEngine(
        steps = mutableListOf<CappedRosterNoticeFactor>(
            RoundingCappedRosterNoticeAdjuster(1.0),
            FlaggedCappedRosterNoticeAdjuster(flags, "capped-roster-notice-fallback", -2.8),
            TieredCappedRosterNoticeAdjuster(
                mutableListOf<Double>(59.0, 7471.0),
                mutableListOf<Double>(0.96, 1.1),
            ),
            CapCappedRosterNoticeAdjuster(45225.0),
            CappedRosterNoticePalletsOffsetAdjuster(1.34),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 35553.6,
    )
}
