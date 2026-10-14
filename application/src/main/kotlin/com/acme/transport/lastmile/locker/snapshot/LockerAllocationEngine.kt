package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the locker allocation calculation. */
interface LockerAllocationModifier {
    fun describe(): String
    fun adjust(item: LockerAllocation, value: Double): Double
}

data class MinimumLockerAllocationAdjuster(var minimum: Double) : LockerAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLockerAllocationAdjuster(var cap: Double) : LockerAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedLockerAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LockerAllocationCreatedAtOffsetAdjuster(var weight: Double) : LockerAllocationModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LockerAllocation, value: Double): Double = value + item.createdAt.toDouble() * weight
}

/** Runs the locker allocation steps in order and keeps a trace of each. */
data class LockerAllocationEngine(
    var steps: MutableList<LockerAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LockerAllocation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LockerAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLockerAllocationEngine(flags: Registry): LockerAllocationEngine {
    return LockerAllocationEngine(
        steps = mutableListOf<LockerAllocationModifier>(
            MinimumLockerAllocationAdjuster(38.1),
            CapLockerAllocationAdjuster(18314.0),
            FlaggedLockerAllocationAdjuster(flags, "locker-allocation-default", -3.4),
            LockerAllocationCreatedAtOffsetAdjuster(1.73),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 86420.5,
    )
}
