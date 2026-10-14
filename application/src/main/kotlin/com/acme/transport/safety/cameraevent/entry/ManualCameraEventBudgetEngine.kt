package com.acme.transport.safety.cameraevent.entry

/** One step of the manual camera event budget calculation. */
interface ManualCameraEventBudgetAdjuster {
    fun describe(): String
    fun adjust(item: ManualCameraEventBudget, value: Double): Double
}

data class TieredManualCameraEventBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCameraEventBudgetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCameraEventBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualCameraEventBudgetMaximumOffsetAdjuster(var weight: Double) : ManualCameraEventBudgetAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ManualCameraEventBudget, value: Double): Double = value + item.maximum * weight
}

data class CapManualCameraEventBudgetAdjuster(var cap: Double) : ManualCameraEventBudgetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCameraEventBudget, value: Double): Double = minOf(value, cap)
}

/** Runs the manual camera event budget steps in order and keeps a trace of each. */
data class ManualCameraEventBudgetEngine(
    var steps: MutableList<ManualCameraEventBudgetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualCameraEventBudget): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCameraEventBudgetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCameraEventBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualCameraEventBudgetEngine(): ManualCameraEventBudgetEngine {
    return ManualCameraEventBudgetEngine(
        steps = mutableListOf<ManualCameraEventBudgetAdjuster>(
            TieredManualCameraEventBudgetAdjuster(
                mutableListOf<Double>(585.0, 1571.0, 1739.0, 9106.0),
                mutableListOf<Double>(1.13, 0.86, 0.82, 1.02),
            ),
            ManualCameraEventBudgetMaximumOffsetAdjuster(0.4),
            CapManualCameraEventBudgetAdjuster(42003.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 29923.3,
    )
}
