package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient availability allocation calculation. */
interface LenientAvailabilityAllocationStep {
    fun describe(): String
    fun adjust(item: LenientAvailabilityAllocation, value: Double): Double
}

data class MinimumLenientAvailabilityAllocationAdjuster(
    var minimum: Double,
) : LenientAvailabilityAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAvailabilityAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientAvailabilityAllocationAdjuster(var step: Double) : LenientAvailabilityAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientAvailabilityAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentLenientAvailabilityAllocationAdjuster(
    var percent: Double,
) : LenientAvailabilityAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAvailabilityAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientAvailabilityAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientAvailabilityAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientAvailabilityAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientAvailabilityAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientAvailabilityAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientAvailabilityAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient availability allocation steps in order and keeps a trace of each. */
data class LenientAvailabilityAllocationEngine(
    var steps: MutableList<LenientAvailabilityAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientAvailabilityAllocation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAvailabilityAllocationStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAvailabilityAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientAvailabilityAllocationEngine(flags: Registry): LenientAvailabilityAllocationEngine {
    return LenientAvailabilityAllocationEngine(
        steps = mutableListOf<LenientAvailabilityAllocationStep>(
            MinimumLenientAvailabilityAllocationAdjuster(30.4),
            RoundingLenientAvailabilityAllocationAdjuster(0.25),
            PercentLenientAvailabilityAllocationAdjuster(-7.7),
            FlaggedLenientAvailabilityAllocationAdjuster(flags, "lenient-availability-allocation-tiered", -9.2),
            TieredLenientAvailabilityAllocationAdjuster(
                mutableListOf<Double>(181.0, 2891.0, 8829.0, 9049.0),
                mutableListOf<Double>(1.11, 1.09, 0.83, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 75652.4,
    )
}
