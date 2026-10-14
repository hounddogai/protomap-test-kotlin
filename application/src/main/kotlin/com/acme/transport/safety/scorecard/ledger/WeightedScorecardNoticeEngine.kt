package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted scorecard notice calculation. */
interface WeightedScorecardNoticeFactor {
    fun describe(): String
    fun adjust(item: WeightedScorecardNotice, value: Double): Double
}

data class MinimumWeightedScorecardNoticeAdjuster(var minimum: Double) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class WeightedScorecardNoticeEtaMinutesOffsetAdjuster(var weight: Double) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class PercentWeightedScorecardNoticeAdjuster(var percent: Double) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingWeightedScorecardNoticeAdjuster(var step: Double) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double = roundTo(value, step)
}

data class LaneWeightedScorecardNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedScorecardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedScorecardNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedScorecardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted scorecard notice steps in order and keeps a trace of each. */
data class WeightedScorecardNoticeEngine(
    var steps: MutableList<WeightedScorecardNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedScorecardNotice): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedScorecardNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedScorecardNoticeEngine(flags: Registry): WeightedScorecardNoticeEngine {
    return WeightedScorecardNoticeEngine(
        steps = mutableListOf<WeightedScorecardNoticeFactor>(
            MinimumWeightedScorecardNoticeAdjuster(46.5),
            WeightedScorecardNoticeEtaMinutesOffsetAdjuster(1.02),
            PercentWeightedScorecardNoticeAdjuster(-14.7),
            RoundingWeightedScorecardNoticeAdjuster(0.1),
            LaneWeightedScorecardNoticeAdjuster(mutableMapOf<String, Double>("east" to 0.87)),
            FlaggedWeightedScorecardNoticeAdjuster(flags, "weighted-scorecard-notice-tiered", 4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 90355.7,
    )
}
