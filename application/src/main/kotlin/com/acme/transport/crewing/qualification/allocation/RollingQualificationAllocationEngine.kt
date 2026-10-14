package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling qualification allocation calculation. */
interface RollingQualificationAllocationAdjuster {
    fun describe(): String
    fun adjust(item: RollingQualificationAllocation, value: Double): Double
}

data class TieredRollingQualificationAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingQualificationAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingQualificationAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingQualificationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingQualificationAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingQualificationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingQualificationAllocationAdjuster(var cap: Double) : RollingQualificationAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingQualificationAllocation, value: Double): Double = minOf(value, cap)
}

data class PercentRollingQualificationAllocationAdjuster(
    var percent: Double,
) : RollingQualificationAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingQualificationAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRollingQualificationAllocationAdjuster(
    var minimum: Double,
) : RollingQualificationAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingQualificationAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling qualification allocation steps in order and keeps a trace of each. */
data class RollingQualificationAllocationEngine(
    var steps: MutableList<RollingQualificationAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingQualificationAllocation): Double {
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

    fun evaluateAll(items: MutableList<RollingQualificationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingQualificationAllocationEngine(flags: Registry): RollingQualificationAllocationEngine {
    return RollingQualificationAllocationEngine(
        steps = mutableListOf<RollingQualificationAllocationAdjuster>(
            TieredRollingQualificationAllocationAdjuster(
                mutableListOf<Double>(1060.0, 1505.0, 2579.0, 4894.0),
                mutableListOf<Double>(0.92, 1.0, 0.97, 1.02),
            ),
            FlaggedRollingQualificationAllocationAdjuster(flags, "rolling-qualification-allocation-capped", 2.7),
            CapRollingQualificationAllocationAdjuster(20066.0),
            PercentRollingQualificationAllocationAdjuster(-19.1),
            MinimumRollingQualificationAllocationAdjuster(45.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 88192.5,
    )
}
