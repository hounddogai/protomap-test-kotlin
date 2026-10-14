package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual clearance checkpoint calculation. */
interface ManualClearanceCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: ManualClearanceCheckpoint, value: Double): Double
}

data class FlaggedManualClearanceCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualClearanceCheckpointAdjuster(var percent: Double) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualClearanceCheckpointAdjuster(var cap: Double) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double = minOf(value, cap)
}

data class ManualClearanceCheckpointThresholdOffsetAdjuster(
    var weight: Double,
) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double = value + item.threshold * weight
}

data class MinimumManualClearanceCheckpointAdjuster(var minimum: Double) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualClearanceCheckpointAdjuster(var step: Double) : ManualClearanceCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualClearanceCheckpoint, value: Double): Double = roundTo(value, step)
}

/** Runs the manual clearance checkpoint steps in order and keeps a trace of each. */
data class ManualClearanceCheckpointEngine(
    var steps: MutableList<ManualClearanceCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualClearanceCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<ManualClearanceCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualClearanceCheckpointEngine(flags: Registry): ManualClearanceCheckpointEngine {
    return ManualClearanceCheckpointEngine(
        steps = mutableListOf<ManualClearanceCheckpointAdjuster>(
            FlaggedManualClearanceCheckpointAdjuster(flags, "manual-clearance-checkpoint-pending", 3.4),
            PercentManualClearanceCheckpointAdjuster(20.6),
            CapManualClearanceCheckpointAdjuster(19798.0),
            ManualClearanceCheckpointThresholdOffsetAdjuster(0.38),
            MinimumManualClearanceCheckpointAdjuster(23.7),
            RoundingManualClearanceCheckpointAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 68202.3,
    )
}
