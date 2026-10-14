package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient hub summary calculation. */
interface LenientHubSummaryModifier {
    fun describe(): String
    fun adjust(item: LenientHubSummary, value: Double): Double
}

data class LenientHubSummaryDurationMinutesOffsetAdjuster(var weight: Double) : LenientHubSummaryModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: LenientHubSummary, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TitleLenientHubSummaryAdjuster(var factors: MutableMap<String, Double>) : LenientHubSummaryModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientHubSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientHubSummaryAdjuster(var percent: Double) : LenientHubSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientHubSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientHubSummaryAdjuster(var step: Double) : LenientHubSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientHubSummary, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientHubSummaryAdjuster(var minimum: Double) : LenientHubSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientHubSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientHubSummaryAdjuster(var cap: Double) : LenientHubSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientHubSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient hub summary steps in order and keeps a trace of each. */
data class LenientHubSummaryEngine(
    var steps: MutableList<LenientHubSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientHubSummary): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientHubSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientHubSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientHubSummaryEngine(): LenientHubSummaryEngine {
    return LenientHubSummaryEngine(
        steps = mutableListOf<LenientHubSummaryModifier>(
            LenientHubSummaryDurationMinutesOffsetAdjuster(1.07),
            TitleLenientHubSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.17, "online" to 0.93)),
            PercentLenientHubSummaryAdjuster(-15.1),
            RoundingLenientHubSummaryAdjuster(0.25),
            MinimumLenientHubSummaryAdjuster(38.2),
            CapLenientHubSummaryAdjuster(22617.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 83260.8,
    )
}
