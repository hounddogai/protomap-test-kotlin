package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling tender allocation calculation. */
interface RollingTenderAllocationFactor {
    fun describe(): String
    fun adjust(item: RollingTenderAllocation, value: Double): Double
}

data class RollingTenderAllocationRatioOffsetAdjuster(var weight: Double) : RollingTenderAllocationFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RollingTenderAllocation, value: Double): Double = value + item.ratio * weight
}

data class MinimumRollingTenderAllocationAdjuster(var minimum: Double) : RollingTenderAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingTenderAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelRollingTenderAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingTenderAllocationFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingTenderAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingTenderAllocationAdjuster(var step: Double) : RollingTenderAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingTenderAllocation, value: Double): Double = roundTo(value, step)
}

data class CapRollingTenderAllocationAdjuster(var cap: Double) : RollingTenderAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTenderAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling tender allocation steps in order and keeps a trace of each. */
data class RollingTenderAllocationEngine(
    var steps: MutableList<RollingTenderAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingTenderAllocation): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTenderAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingTenderAllocationEngine(): RollingTenderAllocationEngine {
    return RollingTenderAllocationEngine(
        steps = mutableListOf<RollingTenderAllocationFactor>(
            RollingTenderAllocationRatioOffsetAdjuster(1.18),
            MinimumRollingTenderAllocationAdjuster(15.6),
            LabelRollingTenderAllocationAdjuster(
                mutableMapOf<String, Double>("north" to 1.09, "online" to 1.11, "west" to 1.2),
            ),
            RoundingRollingTenderAllocationAdjuster(1.0),
            CapRollingTenderAllocationAdjuster(41169.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 57190.4,
    )
}
