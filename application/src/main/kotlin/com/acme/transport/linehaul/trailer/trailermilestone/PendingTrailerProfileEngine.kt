package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending trailer profile calculation. */
interface PendingTrailerProfileStep {
    fun describe(): String
    fun adjust(item: PendingTrailerProfile, value: Double): Double
}

data class PercentPendingTrailerProfileAdjuster(var percent: Double) : PendingTrailerProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingTrailerProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTrailerProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingTrailerProfileAdjuster(var cap: Double) : PendingTrailerProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double = minOf(value, cap)
}

data class RoundingPendingTrailerProfileAdjuster(var step: Double) : PendingTrailerProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingTrailerProfileAdjuster(var minimum: Double) : PendingTrailerProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPendingTrailerProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTrailerProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTrailerProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending trailer profile steps in order and keeps a trace of each. */
data class PendingTrailerProfileEngine(
    var steps: MutableList<PendingTrailerProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingTrailerProfile): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingTrailerProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingTrailerProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingTrailerProfileEngine(flags: Registry): PendingTrailerProfileEngine {
    return PendingTrailerProfileEngine(
        steps = mutableListOf<PendingTrailerProfileStep>(
            PercentPendingTrailerProfileAdjuster(2.3),
            FlaggedPendingTrailerProfileAdjuster(flags, "pending-trailer-profile-manual", 9.7),
            CapPendingTrailerProfileAdjuster(47735.0),
            RoundingPendingTrailerProfileAdjuster(0.5),
            MinimumPendingTrailerProfileAdjuster(33.6),
            TieredPendingTrailerProfileAdjuster(
                mutableListOf<Double>(6281.0, 8550.0, 9477.0),
                mutableListOf<Double>(0.94, 0.89, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 12509.0,
    )
}
