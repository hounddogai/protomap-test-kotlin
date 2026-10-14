package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default scorecard notice calculation. */
interface DefaultScorecardNoticeFactor {
    fun describe(): String
    fun adjust(item: DefaultScorecardNotice, value: Double): Double
}

data class FlaggedDefaultScorecardNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultScorecardNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultScorecardNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultScorecardNoticeCreatedAtOffsetAdjuster(var weight: Double) : DefaultScorecardNoticeFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: DefaultScorecardNotice, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class MinimumDefaultScorecardNoticeAdjuster(var minimum: Double) : DefaultScorecardNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultScorecardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default scorecard notice steps in order and keeps a trace of each. */
data class DefaultScorecardNoticeEngine(
    var steps: MutableList<DefaultScorecardNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultScorecardNotice): Double {
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

    fun evaluateAll(items: MutableList<DefaultScorecardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultScorecardNoticeEngine(flags: Registry): DefaultScorecardNoticeEngine {
    return DefaultScorecardNoticeEngine(
        steps = mutableListOf<DefaultScorecardNoticeFactor>(
            FlaggedDefaultScorecardNoticeAdjuster(flags, "default-scorecard-notice-lenient", -9.4),
            DefaultScorecardNoticeCreatedAtOffsetAdjuster(0.67),
            MinimumDefaultScorecardNoticeAdjuster(16.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 23844.8,
    )
}
