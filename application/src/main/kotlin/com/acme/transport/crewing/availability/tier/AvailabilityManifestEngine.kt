package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the availability manifest calculation. */
interface AvailabilityManifestStep {
    fun describe(): String
    fun adjust(item: AvailabilityManifest, value: Double): Double
}

data class MinimumAvailabilityManifestAdjuster(var minimum: Double) : AvailabilityManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAvailabilityManifestAdjuster(var step: Double) : AvailabilityManifestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityManifest, value: Double): Double = roundTo(value, step)
}

data class FlaggedAvailabilityManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityManifestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameAvailabilityManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityManifestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AvailabilityManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AvailabilityManifestAttemptsOffsetAdjuster(var weight: Double) : AvailabilityManifestStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: AvailabilityManifest, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class CapAvailabilityManifestAdjuster(var cap: Double) : AvailabilityManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the availability manifest steps in order and keeps a trace of each. */
data class AvailabilityManifestEngine(
    var steps: MutableList<AvailabilityManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilityManifest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilityManifestEngine(flags: Registry): AvailabilityManifestEngine {
    return AvailabilityManifestEngine(
        steps = mutableListOf<AvailabilityManifestStep>(
            MinimumAvailabilityManifestAdjuster(28.9),
            RoundingAvailabilityManifestAdjuster(1.0),
            FlaggedAvailabilityManifestAdjuster(flags, "availability-manifest-pending", -6.8),
            NameAvailabilityManifestAdjuster(
                mutableMapOf<String, Double>("north" to 1.16, "retail" to 1.13, "south" to 1.05),
            ),
            AvailabilityManifestAttemptsOffsetAdjuster(1.0),
            CapAvailabilityManifestAdjuster(2594.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 29599.4,
    )
}
