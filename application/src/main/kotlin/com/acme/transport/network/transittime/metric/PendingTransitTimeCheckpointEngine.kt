package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending transit time checkpoint calculation. */
interface PendingTransitTimeCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double
}

data class RoundingPendingTransitTimeCheckpointAdjuster(
    var step: Double,
) : PendingTransitTimeCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingTransitTimeCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingTransitTimeCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPendingTransitTimeCheckpointAdjuster(
    var minimum: Double,
) : PendingTransitTimeCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionPendingTransitTimeCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingTransitTimeCheckpointAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPendingTransitTimeCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingTransitTimeCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingTransitTimeCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending transit time checkpoint steps in order and keeps a trace of each. */
data class PendingTransitTimeCheckpointEngine(
    var steps: MutableList<PendingTransitTimeCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTransitTimeCheckpoint): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTransitTimeCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTransitTimeCheckpointEngine(flags: Registry): PendingTransitTimeCheckpointEngine {
    return PendingTransitTimeCheckpointEngine(
        steps = mutableListOf<PendingTransitTimeCheckpointAdjuster>(
            RoundingPendingTransitTimeCheckpointAdjuster(0.25),
            FlaggedPendingTransitTimeCheckpointAdjuster(flags, "pending-transit-time-checkpoint-rolling", 8.0),
            MinimumPendingTransitTimeCheckpointAdjuster(30.3),
            RegionPendingTransitTimeCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.4, "retail" to 0.88),
            ),
            TieredPendingTransitTimeCheckpointAdjuster(
                mutableListOf<Double>(2489.0, 3428.0, 9863.0),
                mutableListOf<Double>(1.02, 1.02, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 97914.3,
    )
}
