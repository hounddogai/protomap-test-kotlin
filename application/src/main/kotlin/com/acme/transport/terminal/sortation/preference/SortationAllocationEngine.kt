package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation allocation calculation. */
interface SortationAllocationAdjuster {
    fun describe(): String
    fun adjust(item: SortationAllocation, value: Double): Double
}

data class FlaggedSortationAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationAllocationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocaleSortationAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : SortationAllocationAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: SortationAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSortationAllocationAdjuster(var step: Double) : SortationAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationAllocation, value: Double): Double = roundTo(value, step)
}

data class PercentSortationAllocationAdjuster(var percent: Double) : SortationAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the sortation allocation steps in order and keeps a trace of each. */
data class SortationAllocationEngine(
    var steps: MutableList<SortationAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationAllocation): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SortationAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationAllocationEngine(flags: Registry): SortationAllocationEngine {
    return SortationAllocationEngine(
        steps = mutableListOf<SortationAllocationAdjuster>(
            FlaggedSortationAllocationAdjuster(flags, "sortation-allocation-regional", -9.8),
            LocaleSortationAllocationAdjuster(
                mutableMapOf<String, Double>("north" to 0.78, "central" to 1.07, "west" to 0.81),
            ),
            RoundingSortationAllocationAdjuster(0.25),
            PercentSortationAllocationAdjuster(-3.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 60277.5,
    )
}
