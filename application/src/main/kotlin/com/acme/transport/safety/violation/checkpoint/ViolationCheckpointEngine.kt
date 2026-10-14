package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation checkpoint calculation. */
interface ViolationCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: ViolationCheckpoint, value: Double): Double
}

data class SegmentViolationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationCheckpointAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumViolationCheckpointAdjuster(var minimum: Double) : ViolationCheckpointAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredViolationCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapViolationCheckpointAdjuster(var cap: Double) : ViolationCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingViolationCheckpointAdjuster(var step: Double) : ViolationCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentViolationCheckpointAdjuster(var percent: Double) : ViolationCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the violation checkpoint steps in order and keeps a trace of each. */
data class ViolationCheckpointEngine(
    var steps: MutableList<ViolationCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationCheckpoint): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationCheckpointEngine(): ViolationCheckpointEngine {
    return ViolationCheckpointEngine(
        steps = mutableListOf<ViolationCheckpointAdjuster>(
            SegmentViolationCheckpointAdjuster(mutableMapOf<String, Double>("north" to 1.14)),
            MinimumViolationCheckpointAdjuster(18.2),
            TieredViolationCheckpointAdjuster(
                mutableListOf<Double>(1534.0, 6310.0, 8514.0),
                mutableListOf<Double>(0.86, 1.16, 0.83),
            ),
            CapViolationCheckpointAdjuster(3272.0),
            RoundingViolationCheckpointAdjuster(0.5),
            PercentViolationCheckpointAdjuster(6.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 62955.9,
    )
}
