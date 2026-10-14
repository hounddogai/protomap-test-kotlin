package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict duty period allocation calculation. */
interface StrictDutyPeriodAllocationAdjuster {
    fun describe(): String
    fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double
}

data class CapStrictDutyPeriodAllocationAdjuster(var cap: Double) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double = minOf(value, cap)
}

data class StrictDutyPeriodAllocationMinimumOffsetAdjuster(
    var weight: Double,
) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double = value + item.minimum * weight
}

data class RoundingStrictDutyPeriodAllocationAdjuster(var step: Double) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictDutyPeriodAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictDutyPeriodAllocationAdjuster(var minimum: Double) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictDutyPeriodAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDutyPeriodAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDutyPeriodAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict duty period allocation steps in order and keeps a trace of each. */
data class StrictDutyPeriodAllocationEngine(
    var steps: MutableList<StrictDutyPeriodAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictDutyPeriodAllocation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDutyPeriodAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDutyPeriodAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictDutyPeriodAllocationEngine(flags: Registry): StrictDutyPeriodAllocationEngine {
    return StrictDutyPeriodAllocationEngine(
        steps = mutableListOf<StrictDutyPeriodAllocationAdjuster>(
            CapStrictDutyPeriodAllocationAdjuster(28443.0),
            StrictDutyPeriodAllocationMinimumOffsetAdjuster(1.22),
            RoundingStrictDutyPeriodAllocationAdjuster(1.0),
            FlaggedStrictDutyPeriodAllocationAdjuster(flags, "strict-duty-period-allocation-seasonal", 1.5),
            MinimumStrictDutyPeriodAllocationAdjuster(36.8),
            TieredStrictDutyPeriodAllocationAdjuster(
                mutableListOf<Double>(7867.0, 9296.0),
                mutableListOf<Double>(1.01, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 97696.8,
    )
}
