package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.flags.Registry

/** One step of the regional rate card checkpoint calculation. */
interface RegionalRateCardCheckpointStep {
    fun describe(): String
    fun adjust(item: RegionalRateCardCheckpoint, value: Double): Double
}

data class MinimumRegionalRateCardCheckpointAdjuster(var minimum: Double) : RegionalRateCardCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRateCardCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRegionalRateCardCheckpointAdjuster(var cap: Double) : RegionalRateCardCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRateCardCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedRegionalRateCardCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRateCardCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRateCardCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional rate card checkpoint steps in order and keeps a trace of each. */
data class RegionalRateCardCheckpointEngine(
    var steps: MutableList<RegionalRateCardCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalRateCardCheckpoint): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRateCardCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRateCardCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalRateCardCheckpointEngine(flags: Registry): RegionalRateCardCheckpointEngine {
    return RegionalRateCardCheckpointEngine(
        steps = mutableListOf<RegionalRateCardCheckpointStep>(
            MinimumRegionalRateCardCheckpointAdjuster(15.6),
            CapRegionalRateCardCheckpointAdjuster(2105.0),
            FlaggedRegionalRateCardCheckpointAdjuster(flags, "regional-rate-card-checkpoint-default", 5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 8189.9,
    )
}
