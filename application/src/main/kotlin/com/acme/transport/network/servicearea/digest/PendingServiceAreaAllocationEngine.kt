package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.flags.Registry

/** One step of the pending service area allocation calculation. */
interface PendingServiceAreaAllocationFactor {
    fun describe(): String
    fun adjust(item: PendingServiceAreaAllocation, value: Double): Double
}

data class PendingServiceAreaAllocationVersionOffsetAdjuster(
    var weight: Double,
) : PendingServiceAreaAllocationFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PendingServiceAreaAllocation, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class TieredPendingServiceAreaAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingServiceAreaAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingServiceAreaAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingServiceAreaAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingServiceAreaAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingServiceAreaAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingServiceAreaAllocationAdjuster(var cap: Double) : PendingServiceAreaAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingServiceAreaAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the pending service area allocation steps in order and keeps a trace of each. */
data class PendingServiceAreaAllocationEngine(
    var steps: MutableList<PendingServiceAreaAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingServiceAreaAllocation): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingServiceAreaAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingServiceAreaAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingServiceAreaAllocationEngine(flags: Registry): PendingServiceAreaAllocationEngine {
    return PendingServiceAreaAllocationEngine(
        steps = mutableListOf<PendingServiceAreaAllocationFactor>(
            PendingServiceAreaAllocationVersionOffsetAdjuster(0.65),
            TieredPendingServiceAreaAllocationAdjuster(
                mutableListOf<Double>(550.0, 628.0, 1402.0, 4252.0),
                mutableListOf<Double>(0.97, 0.86, 0.93, 1.15),
            ),
            FlaggedPendingServiceAreaAllocationAdjuster(flags, "pending-service-area-allocation-strict", -3.7),
            CapPendingServiceAreaAllocationAdjuster(9765.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 44739.1,
    )
}
