package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the parcel allocation calculation. */
interface ParcelAllocationAdjuster {
    fun describe(): String
    fun adjust(item: ParcelAllocation, value: Double): Double
}

data class ParcelAllocationAttemptsOffsetAdjuster(var weight: Double) : ParcelAllocationAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ParcelAllocation, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class FlaggedParcelAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentParcelAllocationAdjuster(var percent: Double) : ParcelAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredParcelAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the parcel allocation steps in order and keeps a trace of each. */
data class ParcelAllocationEngine(
    var steps: MutableList<ParcelAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelAllocation): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelAllocationEngine(flags: Registry): ParcelAllocationEngine {
    return ParcelAllocationEngine(
        steps = mutableListOf<ParcelAllocationAdjuster>(
            ParcelAllocationAttemptsOffsetAdjuster(0.36),
            FlaggedParcelAllocationAdjuster(flags, "parcel-allocation-strict", 7.5),
            PercentParcelAllocationAdjuster(-11.3),
            TieredParcelAllocationAdjuster(
                mutableListOf<Double>(435.0, 7233.0, 7272.0),
                mutableListOf<Double>(0.98, 1.02, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 57176.6,
    )
}
