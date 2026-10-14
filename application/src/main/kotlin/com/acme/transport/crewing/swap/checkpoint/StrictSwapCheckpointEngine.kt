package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict swap checkpoint calculation. */
interface StrictSwapCheckpointFactor {
    fun describe(): String
    fun adjust(item: StrictSwapCheckpoint, value: Double): Double
}

data class PercentStrictSwapCheckpointAdjuster(var percent: Double) : StrictSwapCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictSwapCheckpointAdjuster(var step: Double) : StrictSwapCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double = roundTo(value, step)
}

data class TieredStrictSwapCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictSwapCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedStrictSwapCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictSwapCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictSwapCheckpointAdjuster(var minimum: Double) : StrictSwapCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionStrictSwapCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictSwapCheckpointFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictSwapCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict swap checkpoint steps in order and keeps a trace of each. */
data class StrictSwapCheckpointEngine(
    var steps: MutableList<StrictSwapCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictSwapCheckpoint): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictSwapCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictSwapCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictSwapCheckpointEngine(flags: Registry): StrictSwapCheckpointEngine {
    return StrictSwapCheckpointEngine(
        steps = mutableListOf<StrictSwapCheckpointFactor>(
            PercentStrictSwapCheckpointAdjuster(7.4),
            RoundingStrictSwapCheckpointAdjuster(0.5),
            TieredStrictSwapCheckpointAdjuster(
                mutableListOf<Double>(3618.0, 4313.0),
                mutableListOf<Double>(0.99, 0.97),
            ),
            FlaggedStrictSwapCheckpointAdjuster(flags, "strict-swap-checkpoint-regional", -8.5),
            MinimumStrictSwapCheckpointAdjuster(29.3),
            RegionStrictSwapCheckpointAdjuster(mutableMapOf<String, Double>("west" to 1.25, "south" to 1.08)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 66122.7,
    )
}
