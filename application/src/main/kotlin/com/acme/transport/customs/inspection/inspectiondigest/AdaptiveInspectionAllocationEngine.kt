package com.acme.transport.customs.inspection.inspectiondigest

/** One step of the adaptive inspection allocation calculation. */
interface AdaptiveInspectionAllocationFactor {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double
}

data class PercentAdaptiveInspectionAllocationAdjuster(
    var percent: Double,
) : AdaptiveInspectionAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveInspectionAllocationStartsAtOffsetAdjuster(
    var weight: Double,
) : AdaptiveInspectionAllocationFactor {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class MinimumAdaptiveInspectionAllocationAdjuster(
    var minimum: Double,
) : AdaptiveInspectionAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveInspectionAllocationAdjuster(var cap: Double) : AdaptiveInspectionAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double = minOf(value, cap)
}

data class NameAdaptiveInspectionAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionAllocationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveInspectionAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive inspection allocation steps in order and keeps a trace of each. */
data class AdaptiveInspectionAllocationEngine(
    var steps: MutableList<AdaptiveInspectionAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveInspectionAllocation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionAllocationEngine(): AdaptiveInspectionAllocationEngine {
    return AdaptiveInspectionAllocationEngine(
        steps = mutableListOf<AdaptiveInspectionAllocationFactor>(
            PercentAdaptiveInspectionAllocationAdjuster(2.3),
            AdaptiveInspectionAllocationStartsAtOffsetAdjuster(1.71),
            MinimumAdaptiveInspectionAllocationAdjuster(12.6),
            CapAdaptiveInspectionAllocationAdjuster(19721.0),
            NameAdaptiveInspectionAllocationAdjuster(
                mutableMapOf<String, Double>("south" to 1.1, "east" to 0.92, "west" to 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 44706.4,
    )
}
