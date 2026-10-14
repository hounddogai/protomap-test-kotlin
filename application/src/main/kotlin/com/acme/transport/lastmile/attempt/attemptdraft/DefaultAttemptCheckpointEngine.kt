package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.flags.Registry

/** One step of the default attempt checkpoint calculation. */
interface DefaultAttemptCheckpointFactor {
    fun describe(): String
    fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double
}

data class TieredDefaultAttemptCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultAttemptCheckpointAdjuster(var cap: Double) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultAttemptCheckpointAdjuster(var minimum: Double) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultAttemptCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryDefaultAttemptCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultAttemptCheckpointAdjuster(var percent: Double) : DefaultAttemptCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultAttemptCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default attempt checkpoint steps in order and keeps a trace of each. */
data class DefaultAttemptCheckpointEngine(
    var steps: MutableList<DefaultAttemptCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultAttemptCheckpoint): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAttemptCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAttemptCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultAttemptCheckpointEngine(flags: Registry): DefaultAttemptCheckpointEngine {
    return DefaultAttemptCheckpointEngine(
        steps = mutableListOf<DefaultAttemptCheckpointFactor>(
            TieredDefaultAttemptCheckpointAdjuster(
                mutableListOf<Double>(1206.0, 3651.0, 5577.0, 5814.0),
                mutableListOf<Double>(1.19, 1.07, 1.09, 1.08),
            ),
            CapDefaultAttemptCheckpointAdjuster(13278.0),
            MinimumDefaultAttemptCheckpointAdjuster(10.9),
            FlaggedDefaultAttemptCheckpointAdjuster(flags, "default-attempt-checkpoint-tiered", -9.4),
            CategoryDefaultAttemptCheckpointAdjuster(mutableMapOf<String, Double>("east" to 1.11)),
            PercentDefaultAttemptCheckpointAdjuster(23.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 26012.5,
    )
}
