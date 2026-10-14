package com.acme.transport.safety.scorecard.scorecardnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped scorecard notice calculation. */
interface CappedScorecardNoticeFactor {
    fun describe(): String
    fun adjust(item: CappedScorecardNotice, value: Double): Double
}

data class LabelCappedScorecardNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedScorecardNoticeFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedScorecardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedScorecardNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedScorecardNoticeAdjuster(var step: Double) : CappedScorecardNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double = roundTo(value, step)
}

data class CapCappedScorecardNoticeAdjuster(var cap: Double) : CappedScorecardNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double = minOf(value, cap)
}

data class PercentCappedScorecardNoticeAdjuster(var percent: Double) : CappedScorecardNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedScorecardNoticeAdjuster(var minimum: Double) : CappedScorecardNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedScorecardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped scorecard notice steps in order and keeps a trace of each. */
data class CappedScorecardNoticeEngine(
    var steps: MutableList<CappedScorecardNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedScorecardNotice): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedScorecardNoticeEngine(flags: Registry): CappedScorecardNoticeEngine {
    return CappedScorecardNoticeEngine(
        steps = mutableListOf<CappedScorecardNoticeFactor>(
            LabelCappedScorecardNoticeAdjuster(mutableMapOf<String, Double>("north" to 1.4)),
            FlaggedCappedScorecardNoticeAdjuster(flags, "capped-scorecard-notice-lenient", 3.0),
            RoundingCappedScorecardNoticeAdjuster(0.1),
            CapCappedScorecardNoticeAdjuster(49980.0),
            PercentCappedScorecardNoticeAdjuster(-9.8),
            MinimumCappedScorecardNoticeAdjuster(42.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 72672.0,
    )
}
