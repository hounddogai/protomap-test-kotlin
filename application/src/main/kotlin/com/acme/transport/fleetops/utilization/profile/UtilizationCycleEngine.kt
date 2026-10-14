package com.acme.transport.fleetops.utilization.profile

/** One step of the utilization cycle calculation. */
interface UtilizationCycleStep {
    fun describe(): String
    fun adjust(item: UtilizationCycle, value: Double): Double
}

data class LabelUtilizationCycleAdjuster(var factors: MutableMap<String, Double>) : UtilizationCycleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: UtilizationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class UtilizationCyclePriorityOffsetAdjuster(var weight: Double) : UtilizationCycleStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: UtilizationCycle, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TieredUtilizationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationCycleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization cycle steps in order and keeps a trace of each. */
data class UtilizationCycleEngine(
    var steps: MutableList<UtilizationCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationCycle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationCycleEngine(): UtilizationCycleEngine {
    return UtilizationCycleEngine(
        steps = mutableListOf<UtilizationCycleStep>(
            LabelUtilizationCycleAdjuster(mutableMapOf<String, Double>("retail" to 1.28, "west" to 0.83)),
            UtilizationCyclePriorityOffsetAdjuster(0.09),
            TieredUtilizationCycleAdjuster(
                mutableListOf<Double>(6667.0, 7017.0, 8683.0),
                mutableListOf<Double>(0.84, 0.91, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 38912.7,
    )
}
