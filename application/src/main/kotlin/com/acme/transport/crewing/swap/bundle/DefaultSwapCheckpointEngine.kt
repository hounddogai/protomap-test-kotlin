package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default swap checkpoint calculation. */
interface DefaultSwapCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: DefaultSwapCheckpoint, value: Double): Double
}

data class FlaggedDefaultSwapCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefaultSwapCheckpointAdjuster(var minimum: Double) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultSwapCheckpointAdjuster(var step: Double) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double = roundTo(value, step)
}

data class SourceDefaultSwapCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultSwapCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultSwapCheckpointAdjuster(var percent: Double) : DefaultSwapCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSwapCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default swap checkpoint steps in order and keeps a trace of each. */
data class DefaultSwapCheckpointEngine(
    var steps: MutableList<DefaultSwapCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultSwapCheckpoint): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultSwapCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultSwapCheckpointEngine(flags: Registry): DefaultSwapCheckpointEngine {
    return DefaultSwapCheckpointEngine(
        steps = mutableListOf<DefaultSwapCheckpointAdjuster>(
            FlaggedDefaultSwapCheckpointAdjuster(flags, "default-swap-checkpoint-batched", -4.0),
            MinimumDefaultSwapCheckpointAdjuster(4.8),
            RoundingDefaultSwapCheckpointAdjuster(0.25),
            SourceDefaultSwapCheckpointAdjuster(mutableMapOf<String, Double>("south" to 1.33, "west" to 1.25)),
            TieredDefaultSwapCheckpointAdjuster(
                mutableListOf<Double>(1303.0, 1379.0, 7872.0, 9585.0),
                mutableListOf<Double>(1.0, 1.05, 0.87, 1.17),
            ),
            PercentDefaultSwapCheckpointAdjuster(-4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 15198.8,
    )
}
