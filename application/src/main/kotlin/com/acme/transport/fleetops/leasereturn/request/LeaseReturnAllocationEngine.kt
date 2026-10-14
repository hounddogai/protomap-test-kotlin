package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lease return allocation calculation. */
interface LeaseReturnAllocationModifier {
    fun describe(): String
    fun adjust(item: LeaseReturnAllocation, value: Double): Double
}

data class PercentLeaseReturnAllocationAdjuster(var percent: Double) : LeaseReturnAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LeaseReturnAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLeaseReturnAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LeaseReturnAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LeaseReturnAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LeaseReturnAllocationLoadKgOffsetAdjuster(var weight: Double) : LeaseReturnAllocationModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LeaseReturnAllocation, value: Double): Double = value + item.loadKg * weight
}

data class RoundingLeaseReturnAllocationAdjuster(var step: Double) : LeaseReturnAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LeaseReturnAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the lease return allocation steps in order and keeps a trace of each. */
data class LeaseReturnAllocationEngine(
    var steps: MutableList<LeaseReturnAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LeaseReturnAllocation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LeaseReturnAllocationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LeaseReturnAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLeaseReturnAllocationEngine(flags: Registry): LeaseReturnAllocationEngine {
    return LeaseReturnAllocationEngine(
        steps = mutableListOf<LeaseReturnAllocationModifier>(
            PercentLeaseReturnAllocationAdjuster(-15.7),
            FlaggedLeaseReturnAllocationAdjuster(flags, "lease-return-allocation-tiered", -8.2),
            LeaseReturnAllocationLoadKgOffsetAdjuster(1.01),
            RoundingLeaseReturnAllocationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 43508.5,
    )
}
