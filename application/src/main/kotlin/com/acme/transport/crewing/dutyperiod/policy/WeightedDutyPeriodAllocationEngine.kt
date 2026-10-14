package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted duty period allocation calculation. */
interface WeightedDutyPeriodAllocationStep {
    fun describe(): String
    fun adjust(item: WeightedDutyPeriodAllocation, value: Double): Double
}

data class WeightedDutyPeriodAllocationSequenceOffsetAdjuster(
    var weight: Double,
) : WeightedDutyPeriodAllocationStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: WeightedDutyPeriodAllocation, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class MinimumWeightedDutyPeriodAllocationAdjuster(var minimum: Double) : WeightedDutyPeriodAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedDutyPeriodAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedDutyPeriodAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedDutyPeriodAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedDutyPeriodAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted duty period allocation steps in order and keeps a trace of each. */
data class WeightedDutyPeriodAllocationEngine(
    var steps: MutableList<WeightedDutyPeriodAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDutyPeriodAllocation): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedDutyPeriodAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDutyPeriodAllocationEngine(flags: Registry): WeightedDutyPeriodAllocationEngine {
    return WeightedDutyPeriodAllocationEngine(
        steps = mutableListOf<WeightedDutyPeriodAllocationStep>(
            WeightedDutyPeriodAllocationSequenceOffsetAdjuster(0.97),
            MinimumWeightedDutyPeriodAllocationAdjuster(7.6),
            FlaggedWeightedDutyPeriodAllocationAdjuster(
                flags,
                "weighted-duty-period-allocation-scheduled",
                -3.8,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 18789.5,
    )
}
