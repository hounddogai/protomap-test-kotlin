package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global attempt checkpoint calculation. */
interface GlobalAttemptCheckpointStep {
    fun describe(): String
    fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double
}

data class RoundingGlobalAttemptCheckpointAdjuster(var step: Double) : GlobalAttemptCheckpointStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double = roundTo(value, step)
}

data class GlobalAttemptCheckpointCeilingOffsetAdjuster(var weight: Double) : GlobalAttemptCheckpointStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double = value + item.ceiling * weight
}

data class TieredGlobalAttemptCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalAttemptCheckpointStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategoryGlobalAttemptCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAttemptCheckpointStep {
    override fun describe(): String = "by category"

    override fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalAttemptCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAttemptCheckpointStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAttemptCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global attempt checkpoint steps in order and keeps a trace of each. */
data class GlobalAttemptCheckpointEngine(
    var steps: MutableList<GlobalAttemptCheckpointStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalAttemptCheckpoint): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalAttemptCheckpointStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalAttemptCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalAttemptCheckpointEngine(flags: Registry): GlobalAttemptCheckpointEngine {
    return GlobalAttemptCheckpointEngine(
        steps = mutableListOf<GlobalAttemptCheckpointStep>(
            RoundingGlobalAttemptCheckpointAdjuster(0.5),
            GlobalAttemptCheckpointCeilingOffsetAdjuster(1.99),
            TieredGlobalAttemptCheckpointAdjuster(
                mutableListOf<Double>(2441.0, 5461.0, 6120.0, 6467.0),
                mutableListOf<Double>(0.91, 1.03, 0.8, 1.13),
            ),
            CategoryGlobalAttemptCheckpointAdjuster(
                mutableMapOf<String, Double>("south" to 0.71, "north" to 0.94),
            ),
            FlaggedGlobalAttemptCheckpointAdjuster(flags, "global-attempt-checkpoint-lenient", -9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 75841.9,
    )
}
