package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling tire checkpoint calculation. */
interface RollingTireCheckpointFactor {
    fun describe(): String
    fun adjust(item: RollingTireCheckpoint, value: Double): Double
}

data class FlaggedRollingTireCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTireCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRollingTireCheckpointAdjuster(var minimum: Double) : RollingTireCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingTireCheckpointAdjuster(var percent: Double) : RollingTireCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingTireCheckpointAdjuster(var cap: Double) : RollingTireCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RegionRollingTireCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTireCheckpointFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingTireCheckpointAdjuster(var step: Double) : RollingTireCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTireCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling tire checkpoint steps in order and keeps a trace of each. */
data class RollingTireCheckpointEngine(
    var steps: MutableList<RollingTireCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingTireCheckpoint): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTireCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingTireCheckpointEngine(flags: Registry): RollingTireCheckpointEngine {
    return RollingTireCheckpointEngine(
        steps = mutableListOf<RollingTireCheckpointFactor>(
            FlaggedRollingTireCheckpointAdjuster(flags, "rolling-tire-checkpoint-lenient", 6.9),
            MinimumRollingTireCheckpointAdjuster(13.0),
            PercentRollingTireCheckpointAdjuster(-19.8),
            CapRollingTireCheckpointAdjuster(36689.0),
            RegionRollingTireCheckpointAdjuster(mutableMapOf<String, Double>("online" to 1.04)),
            RoundingRollingTireCheckpointAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 59161.4,
    )
}
