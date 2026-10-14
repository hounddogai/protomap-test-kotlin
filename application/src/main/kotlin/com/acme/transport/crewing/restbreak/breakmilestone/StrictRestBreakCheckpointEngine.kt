package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict rest break checkpoint calculation. */
interface StrictRestBreakCheckpointFactor {
    fun describe(): String
    fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double
}

data class RoundingStrictRestBreakCheckpointAdjuster(var step: Double) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredStrictRestBreakCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictRestBreakCheckpointMinimumOffsetAdjuster(
    var weight: Double,
) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double = value + item.minimum * weight
}

data class CapStrictRestBreakCheckpointAdjuster(var cap: Double) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double = minOf(value, cap)
}

data class NameStrictRestBreakCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictRestBreakCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRestBreakCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRestBreakCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict rest break checkpoint steps in order and keeps a trace of each. */
data class StrictRestBreakCheckpointEngine(
    var steps: MutableList<StrictRestBreakCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRestBreakCheckpoint): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRestBreakCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRestBreakCheckpointEngine(flags: Registry): StrictRestBreakCheckpointEngine {
    return StrictRestBreakCheckpointEngine(
        steps = mutableListOf<StrictRestBreakCheckpointFactor>(
            RoundingStrictRestBreakCheckpointAdjuster(0.05),
            TieredStrictRestBreakCheckpointAdjuster(
                mutableListOf<Double>(482.0, 957.0, 6325.0),
                mutableListOf<Double>(1.09, 1.13, 0.92),
            ),
            StrictRestBreakCheckpointMinimumOffsetAdjuster(0.36),
            CapStrictRestBreakCheckpointAdjuster(39491.0),
            NameStrictRestBreakCheckpointAdjuster(mutableMapOf<String, Double>("east" to 1.35)),
            FlaggedStrictRestBreakCheckpointAdjuster(flags, "strict-rest-break-checkpoint-manual", -3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 84337.2,
    )
}
