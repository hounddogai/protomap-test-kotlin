package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global tender allocation calculation. */
interface GlobalTenderAllocationAdjuster {
    fun describe(): String
    fun adjust(item: GlobalTenderAllocation, value: Double): Double
}

data class DepotGlobalTenderAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTenderAllocationAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalTenderAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalTenderAllocationAdjuster(var step: Double) : GlobalTenderAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTenderAllocation, value: Double): Double = roundTo(value, step)
}

data class GlobalTenderAllocationRetriesOffsetAdjuster(var weight: Double) : GlobalTenderAllocationAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: GlobalTenderAllocation, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class MinimumGlobalTenderAllocationAdjuster(var minimum: Double) : GlobalTenderAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalTenderAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global tender allocation steps in order and keeps a trace of each. */
data class GlobalTenderAllocationEngine(
    var steps: MutableList<GlobalTenderAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTenderAllocation): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTenderAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTenderAllocationEngine(): GlobalTenderAllocationEngine {
    return GlobalTenderAllocationEngine(
        steps = mutableListOf<GlobalTenderAllocationAdjuster>(
            DepotGlobalTenderAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.19, "west" to 1.01, "east" to 0.77),
            ),
            RoundingGlobalTenderAllocationAdjuster(1.0),
            GlobalTenderAllocationRetriesOffsetAdjuster(1.03),
            MinimumGlobalTenderAllocationAdjuster(2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 58725.2,
    )
}
