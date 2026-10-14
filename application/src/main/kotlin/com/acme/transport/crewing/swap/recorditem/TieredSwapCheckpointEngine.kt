package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered swap checkpoint calculation. */
interface TieredSwapCheckpointStep {
    fun describe(): String
    fun adjust(item: TieredSwapCheckpoint, value: Double): Double
}

data class RoundingTieredSwapCheckpointAdjuster(var step: Double) : TieredSwapCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredSwapCheckpointExpiresAtOffsetAdjuster(var weight: Double) : TieredSwapCheckpointStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class FlaggedTieredSwapCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredSwapCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredSwapCheckpointAdjuster(var cap: Double) : TieredSwapCheckpointStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredSwapCheckpointAdjuster(var minimum: Double) : TieredSwapCheckpointStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTieredSwapCheckpointAdjuster(var percent: Double) : TieredSwapCheckpointStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredSwapCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered swap checkpoint steps in order and keeps a trace of each. */
data class TieredSwapCheckpointEngine(
    var steps: MutableList<TieredSwapCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredSwapCheckpoint): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredSwapCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredSwapCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredSwapCheckpointEngine(flags: Registry): TieredSwapCheckpointEngine {
    return TieredSwapCheckpointEngine(
        steps = mutableListOf<TieredSwapCheckpointStep>(
            RoundingTieredSwapCheckpointAdjuster(0.05),
            TieredSwapCheckpointExpiresAtOffsetAdjuster(0.72),
            FlaggedTieredSwapCheckpointAdjuster(flags, "tiered-swap-checkpoint-scheduled", -4.1),
            CapTieredSwapCheckpointAdjuster(8781.0),
            MinimumTieredSwapCheckpointAdjuster(30.3),
            PercentTieredSwapCheckpointAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 11280.5,
    )
}
