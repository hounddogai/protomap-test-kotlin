package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity target calculation. */
interface CapacityTargetStep {
    fun describe(): String
    fun adjust(item: CapacityTarget, value: Double): Double
}

data class CapacityTargetCostOffsetAdjuster(var weight: Double) : CapacityTargetStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: CapacityTarget, value: Double): Double = value + item.cost * weight
}

data class CapCapacityTargetAdjuster(var cap: Double) : CapacityTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityTarget, value: Double): Double = minOf(value, cap)
}

data class SegmentCapacityTargetAdjuster(var factors: MutableMap<String, Double>) : CapacityTargetStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: CapacityTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCapacityTargetAdjuster(var step: Double) : CapacityTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumCapacityTargetAdjuster(var minimum: Double) : CapacityTargetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capacity target steps in order and keeps a trace of each. */
data class CapacityTargetEngine(
    var steps: MutableList<CapacityTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityTarget): Double {
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

    fun evaluateAll(items: MutableList<CapacityTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityTargetEngine(flags: Registry): CapacityTargetEngine {
    return CapacityTargetEngine(
        steps = mutableListOf<CapacityTargetStep>(
            CapacityTargetCostOffsetAdjuster(0.61),
            CapCapacityTargetAdjuster(48402.0),
            SegmentCapacityTargetAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "online" to 1.28, "retail" to 0.74),
            ),
            RoundingCapacityTargetAdjuster(0.1),
            MinimumCapacityTargetAdjuster(25.8),
            FlaggedCapacityTargetAdjuster(flags, "capacity-target-lenient", -4.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 78111.6,
    )
}
