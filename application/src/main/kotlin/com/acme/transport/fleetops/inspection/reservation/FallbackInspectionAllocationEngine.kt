package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback inspection allocation calculation. */
interface FallbackInspectionAllocationStep {
    fun describe(): String
    fun adjust(item: FallbackInspectionAllocation, value: Double): Double
}

data class FlaggedFallbackInspectionAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackInspectionAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFallbackInspectionAllocationAdjuster(var step: Double) : FallbackInspectionAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackInspectionAllocationAdjuster(var percent: Double) : FallbackInspectionAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackInspectionAllocationAdjuster(var cap: Double) : FallbackInspectionAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double = minOf(value, cap)
}

data class TieredFallbackInspectionAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FallbackInspectionAllocationLimitOffsetAdjuster(
    var weight: Double,
) : FallbackInspectionAllocationStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: FallbackInspectionAllocation, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

/** Runs the fallback inspection allocation steps in order and keeps a trace of each. */
data class FallbackInspectionAllocationEngine(
    var steps: MutableList<FallbackInspectionAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackInspectionAllocation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackInspectionAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackInspectionAllocationEngine(flags: Registry): FallbackInspectionAllocationEngine {
    return FallbackInspectionAllocationEngine(
        steps = mutableListOf<FallbackInspectionAllocationStep>(
            FlaggedFallbackInspectionAllocationAdjuster(flags, "fallback-inspection-allocation-weighted", -7.1),
            RoundingFallbackInspectionAllocationAdjuster(0.25),
            PercentFallbackInspectionAllocationAdjuster(4.6),
            CapFallbackInspectionAllocationAdjuster(36542.0),
            TieredFallbackInspectionAllocationAdjuster(
                mutableListOf<Double>(2153.0, 9068.0),
                mutableListOf<Double>(1.0, 0.83),
            ),
            FallbackInspectionAllocationLimitOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 18887.8,
    )
}
