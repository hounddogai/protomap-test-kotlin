package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer checkpoint calculation. */
interface TrailerCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: TrailerCheckpoint, value: Double): Double
}

data class CapTrailerCheckpointAdjuster(var cap: Double) : TrailerCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerCheckpoint, value: Double): Double = minOf(value, cap)
}

data class FlaggedTrailerCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerCheckpointEtaMinutesOffsetAdjuster(var weight: Double) : TrailerCheckpointAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: TrailerCheckpoint, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class RoundingTrailerCheckpointAdjuster(var step: Double) : TrailerCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerCheckpoint, value: Double): Double = roundTo(value, step)
}

data class LabelTrailerCheckpointAdjuster(var factors: MutableMap<String, Double>) : TrailerCheckpointAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TrailerCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the trailer checkpoint steps in order and keeps a trace of each. */
data class TrailerCheckpointEngine(
    var steps: MutableList<TrailerCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerCheckpoint): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerCheckpointEngine(flags: Registry): TrailerCheckpointEngine {
    return TrailerCheckpointEngine(
        steps = mutableListOf<TrailerCheckpointAdjuster>(
            CapTrailerCheckpointAdjuster(42855.0),
            FlaggedTrailerCheckpointAdjuster(flags, "trailer-checkpoint-pending", 4.5),
            TrailerCheckpointEtaMinutesOffsetAdjuster(0.75),
            RoundingTrailerCheckpointAdjuster(1.0),
            LabelTrailerCheckpointAdjuster(
                mutableMapOf<String, Double>("south" to 1.01, "east" to 1.15, "retail" to 0.75),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 35345.2,
    )
}
