package com.acme.transport.safety.cameraevent.budget

/** One step of the regional camera event budget calculation. */
interface RegionalCameraEventBudgetStep {
    fun describe(): String
    fun adjust(item: RegionalCameraEventBudget, value: Double): Double
}

data class ChannelRegionalCameraEventBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCameraEventBudgetStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalCameraEventBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalCameraEventBudgetAdjuster(var minimum: Double) : RegionalCameraEventBudgetStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCameraEventBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalCameraEventBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCameraEventBudgetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCameraEventBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalCameraEventBudgetBudgetOffsetAdjuster(var weight: Double) : RegionalCameraEventBudgetStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RegionalCameraEventBudget, value: Double): Double = value + item.budget * weight
}

/** Runs the regional camera event budget steps in order and keeps a trace of each. */
data class RegionalCameraEventBudgetEngine(
    var steps: MutableList<RegionalCameraEventBudgetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCameraEventBudget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCameraEventBudgetStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCameraEventBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCameraEventBudgetEngine(): RegionalCameraEventBudgetEngine {
    return RegionalCameraEventBudgetEngine(
        steps = mutableListOf<RegionalCameraEventBudgetStep>(
            ChannelRegionalCameraEventBudgetAdjuster(mutableMapOf<String, Double>("west" to 0.74)),
            MinimumRegionalCameraEventBudgetAdjuster(8.2),
            TieredRegionalCameraEventBudgetAdjuster(
                mutableListOf<Double>(4517.0, 5221.0, 5329.0, 8294.0),
                mutableListOf<Double>(0.9, 0.92, 1.02, 1.2),
            ),
            RegionalCameraEventBudgetBudgetOffsetAdjuster(1.88),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 83622.7,
    )
}
