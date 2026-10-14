package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal relay notice calculation. */
interface SeasonalRelayNoticeStep {
    fun describe(): String
    fun adjust(item: SeasonalRelayNotice, value: Double): Double
}

data class MinimumSeasonalRelayNoticeAdjuster(var minimum: Double) : SeasonalRelayNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalRelayNoticeAdjuster(var step: Double) : SeasonalRelayNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double = roundTo(value, step)
}

data class LabelSeasonalRelayNoticeAdjuster(var factors: MutableMap<String, Double>) : SeasonalRelayNoticeStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSeasonalRelayNoticeAdjuster(var percent: Double) : SeasonalRelayNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalRelayNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRelayNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SeasonalRelayNoticeDiscountOffsetAdjuster(var weight: Double) : SeasonalRelayNoticeStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: SeasonalRelayNotice, value: Double): Double = value + item.discount * weight
}

/** Runs the seasonal relay notice steps in order and keeps a trace of each. */
data class SeasonalRelayNoticeEngine(
    var steps: MutableList<SeasonalRelayNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalRelayNotice): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalRelayNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalRelayNoticeEngine(flags: Registry): SeasonalRelayNoticeEngine {
    return SeasonalRelayNoticeEngine(
        steps = mutableListOf<SeasonalRelayNoticeStep>(
            MinimumSeasonalRelayNoticeAdjuster(43.5),
            RoundingSeasonalRelayNoticeAdjuster(5.0),
            LabelSeasonalRelayNoticeAdjuster(mutableMapOf<String, Double>("south" to 1.21)),
            PercentSeasonalRelayNoticeAdjuster(-9.2),
            FlaggedSeasonalRelayNoticeAdjuster(flags, "seasonal-relay-notice-default", 2.4),
            SeasonalRelayNoticeDiscountOffsetAdjuster(0.34),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 22554.1,
    )
}
