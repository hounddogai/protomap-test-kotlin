package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the duty period allocation calculation. */
interface DutyPeriodAllocationAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodAllocation, value: Double): Double
}

data class FlaggedDutyPeriodAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DutyPeriodAllocationExpiresAtOffsetAdjuster(var weight: Double) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class PercentDutyPeriodAllocationAdjuster(var percent: Double) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryDutyPeriodAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDutyPeriodAllocationAdjuster(var step: Double) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumDutyPeriodAllocationAdjuster(var minimum: Double) : DutyPeriodAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DutyPeriodAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the duty period allocation steps in order and keeps a trace of each. */
data class DutyPeriodAllocationEngine(
    var steps: MutableList<DutyPeriodAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DutyPeriodAllocation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DutyPeriodAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DutyPeriodAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDutyPeriodAllocationEngine(flags: Registry): DutyPeriodAllocationEngine {
    return DutyPeriodAllocationEngine(
        steps = mutableListOf<DutyPeriodAllocationAdjuster>(
            FlaggedDutyPeriodAllocationAdjuster(flags, "duty-period-allocation-capped", -3.1),
            DutyPeriodAllocationExpiresAtOffsetAdjuster(1.57),
            PercentDutyPeriodAllocationAdjuster(14.6),
            CategoryDutyPeriodAllocationAdjuster(mutableMapOf<String, Double>("east" to 0.79)),
            RoundingDutyPeriodAllocationAdjuster(0.1),
            MinimumDutyPeriodAllocationAdjuster(21.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 8534.1,
    )
}
