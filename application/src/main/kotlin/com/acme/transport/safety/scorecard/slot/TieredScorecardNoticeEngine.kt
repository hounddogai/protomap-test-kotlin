package com.acme.transport.safety.scorecard.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered scorecard notice calculation. */
interface TieredScorecardNoticeFactor {
    fun describe(): String
    fun adjust(item: TieredScorecardNotice, value: Double): Double
}

data class RoundingTieredScorecardNoticeAdjuster(var step: Double) : TieredScorecardNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double = roundTo(value, step)
}

data class CapTieredScorecardNoticeAdjuster(var cap: Double) : TieredScorecardNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double = minOf(value, cap)
}

data class TieredScorecardNoticeStartsAtOffsetAdjuster(var weight: Double) : TieredScorecardNoticeFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class MinimumTieredScorecardNoticeAdjuster(var minimum: Double) : TieredScorecardNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredScorecardNoticeAdjuster(var percent: Double) : TieredScorecardNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTieredScorecardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredScorecardNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredScorecardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered scorecard notice steps in order and keeps a trace of each. */
data class TieredScorecardNoticeEngine(
    var steps: MutableList<TieredScorecardNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredScorecardNotice): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredScorecardNoticeEngine(flags: Registry): TieredScorecardNoticeEngine {
    return TieredScorecardNoticeEngine(
        steps = mutableListOf<TieredScorecardNoticeFactor>(
            RoundingTieredScorecardNoticeAdjuster(5.0),
            CapTieredScorecardNoticeAdjuster(8361.0),
            TieredScorecardNoticeStartsAtOffsetAdjuster(0.79),
            MinimumTieredScorecardNoticeAdjuster(31.9),
            PercentTieredScorecardNoticeAdjuster(-8.3),
            FlaggedTieredScorecardNoticeAdjuster(flags, "tiered-scorecard-notice-legacy", 5.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 64931.2,
    )
}
