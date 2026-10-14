package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled hold allocation calculation. */
interface ScheduledHoldAllocationStep {
    fun describe(): String
    fun adjust(item: ScheduledHoldAllocation, value: Double): Double
}

data class MinimumScheduledHoldAllocationAdjuster(var minimum: Double) : ScheduledHoldAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledHoldAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledHoldAllocationAdjuster(var step: Double) : ScheduledHoldAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledHoldAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedScheduledHoldAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHoldAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHoldAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hold allocation steps in order and keeps a trace of each. */
data class ScheduledHoldAllocationEngine(
    var steps: MutableList<ScheduledHoldAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledHoldAllocation): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledHoldAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledHoldAllocationEngine(flags: Registry): ScheduledHoldAllocationEngine {
    return ScheduledHoldAllocationEngine(
        steps = mutableListOf<ScheduledHoldAllocationStep>(
            MinimumScheduledHoldAllocationAdjuster(26.4),
            RoundingScheduledHoldAllocationAdjuster(0.1),
            FlaggedScheduledHoldAllocationAdjuster(flags, "scheduled-hold-allocation-fallback", 4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 95120.1,
    )
}
