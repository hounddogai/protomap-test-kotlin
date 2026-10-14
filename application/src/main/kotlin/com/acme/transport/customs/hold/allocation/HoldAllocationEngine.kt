package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the hold allocation calculation. */
interface HoldAllocationStep {
    fun describe(): String
    fun adjust(item: HoldAllocation, value: Double): Double
}

data class LaneHoldAllocationAdjuster(var factors: MutableMap<String, Double>) : HoldAllocationStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: HoldAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapHoldAllocationAdjuster(var cap: Double) : HoldAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HoldAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedHoldAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hold allocation steps in order and keeps a trace of each. */
data class HoldAllocationEngine(
    var steps: MutableList<HoldAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldAllocation): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<HoldAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldAllocationEngine(flags: Registry): HoldAllocationEngine {
    return HoldAllocationEngine(
        steps = mutableListOf<HoldAllocationStep>(
            LaneHoldAllocationAdjuster(mutableMapOf<String, Double>("east" to 1.26, "west" to 0.71)),
            CapHoldAllocationAdjuster(39225.0),
            FlaggedHoldAllocationAdjuster(flags, "hold-allocation-rolling", -8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 15863.3,
    )
}
