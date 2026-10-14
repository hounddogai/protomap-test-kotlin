package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard checkpoint calculation. */
interface TrailerYardCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardCheckpoint, value: Double): Double
}

data class TrailerYardCheckpointLoadKgOffsetAdjuster(var weight: Double) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double = value + item.loadKg * weight
}

data class CapTrailerYardCheckpointAdjuster(var cap: Double) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double = minOf(value, cap)
}

data class PercentTrailerYardCheckpointAdjuster(var percent: Double) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelTrailerYardCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTrailerYardCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingTrailerYardCheckpointAdjuster(var step: Double) : TrailerYardCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer yard checkpoint steps in order and keeps a trace of each. */
data class TrailerYardCheckpointEngine(
    var steps: MutableList<TrailerYardCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerYardCheckpoint): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerYardCheckpointEngine(flags: Registry): TrailerYardCheckpointEngine {
    return TrailerYardCheckpointEngine(
        steps = mutableListOf<TrailerYardCheckpointAdjuster>(
            TrailerYardCheckpointLoadKgOffsetAdjuster(0.09),
            CapTrailerYardCheckpointAdjuster(40764.0),
            PercentTrailerYardCheckpointAdjuster(2.2),
            LabelTrailerYardCheckpointAdjuster(mutableMapOf<String, Double>("online" to 1.07, "retail" to 1.4)),
            FlaggedTrailerYardCheckpointAdjuster(flags, "trailer-yard-checkpoint-global", -6.8),
            RoundingTrailerYardCheckpointAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 96171.7,
    )
}
