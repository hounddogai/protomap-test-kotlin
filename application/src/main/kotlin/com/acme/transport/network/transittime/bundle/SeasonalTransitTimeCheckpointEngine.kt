package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal transit time checkpoint calculation. */
interface SeasonalTransitTimeCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double
}

data class SeasonalTransitTimeCheckpointRatioOffsetAdjuster(
    var weight: Double,
) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double = value + item.ratio * weight
}

data class CapSeasonalTransitTimeCheckpointAdjuster(var cap: Double) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TitleSeasonalTransitTimeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalTransitTimeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalTransitTimeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalTransitTimeCheckpointAdjuster(
    var step: Double,
) : SeasonalTransitTimeCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTransitTimeCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal transit time checkpoint steps in order and keeps a trace of each. */
data class SeasonalTransitTimeCheckpointEngine(
    var steps: MutableList<SeasonalTransitTimeCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalTransitTimeCheckpoint): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalTransitTimeCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalTransitTimeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalTransitTimeCheckpointEngine(flags: Registry): SeasonalTransitTimeCheckpointEngine {
    return SeasonalTransitTimeCheckpointEngine(
        steps = mutableListOf<SeasonalTransitTimeCheckpointAdjuster>(
            SeasonalTransitTimeCheckpointRatioOffsetAdjuster(0.75),
            CapSeasonalTransitTimeCheckpointAdjuster(8460.0),
            TitleSeasonalTransitTimeCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.34, "west" to 0.75, "east" to 1.24),
            ),
            FlaggedSeasonalTransitTimeCheckpointAdjuster(
                flags,
                "seasonal-transit-time-checkpoint-scheduled",
                8.5,
            ),
            TieredSeasonalTransitTimeCheckpointAdjuster(
                mutableListOf<Double>(306.0, 6713.0, 8600.0, 9850.0),
                mutableListOf<Double>(1.08, 0.97, 0.82, 1.15),
            ),
            RoundingSeasonalTransitTimeCheckpointAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 86871.4,
    )
}
