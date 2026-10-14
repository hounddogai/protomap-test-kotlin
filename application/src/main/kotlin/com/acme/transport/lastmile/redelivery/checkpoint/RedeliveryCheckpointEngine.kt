package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the redelivery checkpoint calculation. */
interface RedeliveryCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryCheckpoint, value: Double): Double
}

data class LaneRedeliveryCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryCheckpointAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RedeliveryCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RedeliveryCheckpointGraceDaysOffsetAdjuster(var weight: Double) : RedeliveryCheckpointAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: RedeliveryCheckpoint, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedRedeliveryCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRedeliveryCheckpointAdjuster(var minimum: Double) : RedeliveryCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the redelivery checkpoint steps in order and keeps a trace of each. */
data class RedeliveryCheckpointEngine(
    var steps: MutableList<RedeliveryCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryCheckpoint): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryCheckpointEngine(flags: Registry): RedeliveryCheckpointEngine {
    return RedeliveryCheckpointEngine(
        steps = mutableListOf<RedeliveryCheckpointAdjuster>(
            LaneRedeliveryCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "west" to 1.26, "online" to 1.33),
            ),
            RedeliveryCheckpointGraceDaysOffsetAdjuster(1.3),
            FlaggedRedeliveryCheckpointAdjuster(flags, "redelivery-checkpoint-seasonal", 7.6),
            MinimumRedeliveryCheckpointAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 21129.6,
    )
}
