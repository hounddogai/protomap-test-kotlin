package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending availability score calculation. */
interface PendingAvailabilityScoreStep {
    fun describe(): String
    fun adjust(item: PendingAvailabilityScore, value: Double): Double
}

data class PendingAvailabilityScoreStopCountOffsetAdjuster(var weight: Double) : PendingAvailabilityScoreStep {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class PercentPendingAvailabilityScoreAdjuster(var percent: Double) : PendingAvailabilityScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingAvailabilityScoreAdjuster(var step: Double) : PendingAvailabilityScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double = roundTo(value, step)
}

data class TitlePendingAvailabilityScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingAvailabilityScoreStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingAvailabilityScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAvailabilityScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingAvailabilityScoreAdjuster(var cap: Double) : PendingAvailabilityScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingAvailabilityScore, value: Double): Double = minOf(value, cap)
}

/** Runs the pending availability score steps in order and keeps a trace of each. */
data class PendingAvailabilityScoreEngine(
    var steps: MutableList<PendingAvailabilityScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingAvailabilityScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAvailabilityScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAvailabilityScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingAvailabilityScoreEngine(flags: Registry): PendingAvailabilityScoreEngine {
    return PendingAvailabilityScoreEngine(
        steps = mutableListOf<PendingAvailabilityScoreStep>(
            PendingAvailabilityScoreStopCountOffsetAdjuster(1.4),
            PercentPendingAvailabilityScoreAdjuster(-15.0),
            RoundingPendingAvailabilityScoreAdjuster(0.25),
            TitlePendingAvailabilityScoreAdjuster(
                mutableMapOf<String, Double>("online" to 1.02, "central" to 0.89, "east" to 1.14),
            ),
            FlaggedPendingAvailabilityScoreAdjuster(flags, "pending-availability-score-seasonal", -7.8),
            CapPendingAvailabilityScoreAdjuster(49101.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 61118.9,
    )
}
