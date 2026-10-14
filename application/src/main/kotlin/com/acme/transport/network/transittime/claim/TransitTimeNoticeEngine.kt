package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.numbers.roundTo

/** One step of the transit time notice calculation. */
interface TransitTimeNoticeStep {
    fun describe(): String
    fun adjust(item: TransitTimeNotice, value: Double): Double
}

data class RoundingTransitTimeNoticeAdjuster(var step: Double) : TransitTimeNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeNotice, value: Double): Double = roundTo(value, step)
}

data class TitleTransitTimeNoticeAdjuster(var factors: MutableMap<String, Double>) : TransitTimeNoticeStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TransitTimeNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTransitTimeNoticeAdjuster(var minimum: Double) : TransitTimeNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TransitTimeNoticeDurationMinutesOffsetAdjuster(var weight: Double) : TransitTimeNoticeStep {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: TransitTimeNotice, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapTransitTimeNoticeAdjuster(var cap: Double) : TransitTimeNoticeStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeNotice, value: Double): Double = minOf(value, cap)
}

data class PercentTransitTimeNoticeAdjuster(var percent: Double) : TransitTimeNoticeStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the transit time notice steps in order and keeps a trace of each. */
data class TransitTimeNoticeEngine(
    var steps: MutableList<TransitTimeNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeNoticeEngine(): TransitTimeNoticeEngine {
    return TransitTimeNoticeEngine(
        steps = mutableListOf<TransitTimeNoticeStep>(
            RoundingTransitTimeNoticeAdjuster(1.0),
            TitleTransitTimeNoticeAdjuster(mutableMapOf<String, Double>("retail" to 1.08, "east" to 0.95)),
            MinimumTransitTimeNoticeAdjuster(4.8),
            TransitTimeNoticeDurationMinutesOffsetAdjuster(1.44),
            CapTransitTimeNoticeAdjuster(12393.0),
            PercentTransitTimeNoticeAdjuster(-18.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 38068.3,
    )
}
