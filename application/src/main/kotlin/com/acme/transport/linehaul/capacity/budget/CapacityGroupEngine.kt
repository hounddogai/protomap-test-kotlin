package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity group calculation. */
interface CapacityGroupAdjuster {
    fun describe(): String
    fun adjust(item: CapacityGroup, value: Double): Double
}

data class CapCapacityGroupAdjuster(var cap: Double) : CapacityGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityGroup, value: Double): Double = minOf(value, cap)
}

data class PercentCapacityGroupAdjuster(var percent: Double) : CapacityGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapacityGroupRatioOffsetAdjuster(var weight: Double) : CapacityGroupAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: CapacityGroup, value: Double): Double = value + item.ratio * weight
}

data class TieredCapacityGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCapacityGroupAdjuster(var minimum: Double) : CapacityGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capacity group steps in order and keeps a trace of each. */
data class CapacityGroupEngine(
    var steps: MutableList<CapacityGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CapacityGroup): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCapacityGroupEngine(flags: Registry): CapacityGroupEngine {
    return CapacityGroupEngine(
        steps = mutableListOf<CapacityGroupAdjuster>(
            CapCapacityGroupAdjuster(14009.0),
            PercentCapacityGroupAdjuster(18.3),
            CapacityGroupRatioOffsetAdjuster(1.35),
            TieredCapacityGroupAdjuster(
                mutableListOf<Double>(5813.0, 9124.0, 9589.0),
                mutableListOf<Double>(1.06, 1.06, 1.14),
            ),
            MinimumCapacityGroupAdjuster(20.3),
            FlaggedCapacityGroupAdjuster(flags, "capacity-group-batched", -0.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 66796.7,
    )
}
