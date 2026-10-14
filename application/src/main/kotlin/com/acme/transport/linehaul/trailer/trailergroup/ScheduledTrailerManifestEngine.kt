package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled trailer manifest calculation. */
interface ScheduledTrailerManifestFactor {
    fun describe(): String
    fun adjust(item: ScheduledTrailerManifest, value: Double): Double
}

data class RoundingScheduledTrailerManifestAdjuster(var step: Double) : ScheduledTrailerManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledTrailerManifest, value: Double): Double = roundTo(value, step)
}

data class CapScheduledTrailerManifestAdjuster(var cap: Double) : ScheduledTrailerManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTrailerManifest, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledTrailerManifestAdjuster(var percent: Double) : ScheduledTrailerManifestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledTrailerManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledTrailerManifestAdjuster(var minimum: Double) : ScheduledTrailerManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledTrailerManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled trailer manifest steps in order and keeps a trace of each. */
data class ScheduledTrailerManifestEngine(
    var steps: MutableList<ScheduledTrailerManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledTrailerManifest): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTrailerManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTrailerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledTrailerManifestEngine(): ScheduledTrailerManifestEngine {
    return ScheduledTrailerManifestEngine(
        steps = mutableListOf<ScheduledTrailerManifestFactor>(
            RoundingScheduledTrailerManifestAdjuster(0.05),
            CapScheduledTrailerManifestAdjuster(21485.0),
            PercentScheduledTrailerManifestAdjuster(-10.1),
            MinimumScheduledTrailerManifestAdjuster(18.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 16080.7,
    )
}
