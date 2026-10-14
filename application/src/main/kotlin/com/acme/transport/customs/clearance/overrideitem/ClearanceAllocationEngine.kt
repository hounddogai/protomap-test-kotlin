package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance allocation calculation. */
interface ClearanceAllocationFactor {
    fun describe(): String
    fun adjust(item: ClearanceAllocation, value: Double): Double
}

data class RoundingClearanceAllocationAdjuster(var step: Double) : ClearanceAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumClearanceAllocationAdjuster(var minimum: Double) : ClearanceAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapClearanceAllocationAdjuster(var cap: Double) : ClearanceAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceAllocation, value: Double): Double = minOf(value, cap)
}

data class FlaggedClearanceAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredClearanceAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance allocation steps in order and keeps a trace of each. */
data class ClearanceAllocationEngine(
    var steps: MutableList<ClearanceAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceAllocation): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceAllocationEngine(flags: Registry): ClearanceAllocationEngine {
    return ClearanceAllocationEngine(
        steps = mutableListOf<ClearanceAllocationFactor>(
            RoundingClearanceAllocationAdjuster(0.05),
            MinimumClearanceAllocationAdjuster(10.1),
            CapClearanceAllocationAdjuster(13444.0),
            FlaggedClearanceAllocationAdjuster(flags, "clearance-allocation-batched", 9.1),
            TieredClearanceAllocationAdjuster(
                mutableListOf<Double>(1090.0, 4778.0, 5782.0, 6229.0),
                mutableListOf<Double>(1.04, 1.19, 1.04, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 28800.4,
    )
}
