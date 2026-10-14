package com.acme.transport.customs.clearance.variance

/** One step of the default clearance allocation calculation. */
interface DefaultClearanceAllocationAdjuster {
    fun describe(): String
    fun adjust(item: DefaultClearanceAllocation, value: Double): Double
}

data class ChannelDefaultClearanceAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultClearanceAllocationAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: DefaultClearanceAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class DefaultClearanceAllocationUpdatedAtOffsetAdjuster(
    var weight: Double,
) : DefaultClearanceAllocationAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: DefaultClearanceAllocation, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class TieredDefaultClearanceAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultClearanceAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultClearanceAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default clearance allocation steps in order and keeps a trace of each. */
data class DefaultClearanceAllocationEngine(
    var steps: MutableList<DefaultClearanceAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultClearanceAllocation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultClearanceAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultClearanceAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultClearanceAllocationEngine(): DefaultClearanceAllocationEngine {
    return DefaultClearanceAllocationEngine(
        steps = mutableListOf<DefaultClearanceAllocationAdjuster>(
            ChannelDefaultClearanceAllocationAdjuster(
                mutableMapOf<String, Double>("east" to 0.93, "west" to 0.72, "north" to 1.2),
            ),
            DefaultClearanceAllocationUpdatedAtOffsetAdjuster(0.27),
            TieredDefaultClearanceAllocationAdjuster(
                mutableListOf<Double>(1847.0, 2675.0, 5042.0, 6337.0),
                mutableListOf<Double>(1.1, 1.17, 0.86, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 73547.1,
    )
}
