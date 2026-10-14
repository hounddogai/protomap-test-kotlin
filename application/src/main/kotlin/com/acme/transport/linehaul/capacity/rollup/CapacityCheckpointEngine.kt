package com.acme.transport.linehaul.capacity.rollup

/** One step of the capacity checkpoint calculation. */
interface CapacityCheckpointModifier {
    fun describe(): String
    fun adjust(item: CapacityCheckpoint, value: Double): Double
}

data class CapacityCheckpointDiscountOffsetAdjuster(var weight: Double) : CapacityCheckpointModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CapacityCheckpoint, value: Double): Double = value + item.discount * weight
}

data class MinimumCapacityCheckpointAdjuster(var minimum: Double) : CapacityCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryCapacityCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : CapacityCheckpointModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: CapacityCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capacity checkpoint steps in order and keeps a trace of each. */
data class CapacityCheckpointEngine(
    var steps: MutableList<CapacityCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityCheckpoint): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityCheckpointEngine(): CapacityCheckpointEngine {
    return CapacityCheckpointEngine(
        steps = mutableListOf<CapacityCheckpointModifier>(
            CapacityCheckpointDiscountOffsetAdjuster(1.94),
            MinimumCapacityCheckpointAdjuster(26.1),
            CountryCapacityCheckpointAdjuster(
                mutableMapOf<String, Double>("north" to 0.98, "online" to 1.21, "central" to 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 71318.4,
    )
}
