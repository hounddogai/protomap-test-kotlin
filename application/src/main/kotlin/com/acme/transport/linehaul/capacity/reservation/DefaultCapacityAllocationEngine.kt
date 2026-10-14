package com.acme.transport.linehaul.capacity.reservation

/** One step of the default capacity allocation calculation. */
interface DefaultCapacityAllocationAdjuster {
    fun describe(): String
    fun adjust(item: DefaultCapacityAllocation, value: Double): Double
}

data class DefaultCapacityAllocationFloorValueOffsetAdjuster(
    var weight: Double,
) : DefaultCapacityAllocationAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: DefaultCapacityAllocation, value: Double): Double = value + item.floorValue * weight
}

data class MinimumDefaultCapacityAllocationAdjuster(var minimum: Double) : DefaultCapacityAllocationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCapacityAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultCapacityAllocationAdjuster(var percent: Double) : DefaultCapacityAllocationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCapacityAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default capacity allocation steps in order and keeps a trace of each. */
data class DefaultCapacityAllocationEngine(
    var steps: MutableList<DefaultCapacityAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCapacityAllocation): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCapacityAllocationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCapacityAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCapacityAllocationEngine(): DefaultCapacityAllocationEngine {
    return DefaultCapacityAllocationEngine(
        steps = mutableListOf<DefaultCapacityAllocationAdjuster>(
            DefaultCapacityAllocationFloorValueOffsetAdjuster(1.81),
            MinimumDefaultCapacityAllocationAdjuster(8.9),
            PercentDefaultCapacityAllocationAdjuster(14.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 24862.5,
    )
}
