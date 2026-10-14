package com.acme.transport.linehaul.relay.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the relay summary calculation. */
interface RelaySummaryModifier {
    fun describe(): String
    fun adjust(item: RelaySummary, value: Double): Double
}

data class RelaySummaryQuantityOffsetAdjuster(var weight: Double) : RelaySummaryModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RelaySummary, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class MinimumRelaySummaryAdjuster(var minimum: Double) : RelaySummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelaySummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelRelaySummaryAdjuster(var factors: MutableMap<String, Double>) : RelaySummaryModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: RelaySummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRelaySummaryAdjuster(var percent: Double) : RelaySummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelaySummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRelaySummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelaySummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelaySummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRelaySummaryAdjuster(var step: Double) : RelaySummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelaySummary, value: Double): Double = roundTo(value, step)
}

/** Runs the relay summary steps in order and keeps a trace of each. */
data class RelaySummaryEngine(
    var steps: MutableList<RelaySummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelaySummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelaySummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelaySummaryEngine(flags: Registry): RelaySummaryEngine {
    return RelaySummaryEngine(
        steps = mutableListOf<RelaySummaryModifier>(
            RelaySummaryQuantityOffsetAdjuster(0.72),
            MinimumRelaySummaryAdjuster(6.3),
            ChannelRelaySummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.33, "east" to 1.23)),
            PercentRelaySummaryAdjuster(-22.7),
            FlaggedRelaySummaryAdjuster(flags, "relay-summary-pending", 4.9),
            RoundingRelaySummaryAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 96783.4,
    )
}
