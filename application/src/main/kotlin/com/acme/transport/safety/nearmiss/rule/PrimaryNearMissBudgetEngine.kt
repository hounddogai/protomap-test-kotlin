package com.acme.transport.safety.nearmiss.rule

/** One step of the primary near miss budget calculation. */
interface PrimaryNearMissBudgetModifier {
    fun describe(): String
    fun adjust(item: PrimaryNearMissBudget, value: Double): Double
}

data class CapPrimaryNearMissBudgetAdjuster(var cap: Double) : PrimaryNearMissBudgetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryNearMissBudget, value: Double): Double = minOf(value, cap)
}

data class CategoryPrimaryNearMissBudgetAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryNearMissBudgetModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: PrimaryNearMissBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryNearMissBudgetAdjuster(var minimum: Double) : PrimaryNearMissBudgetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryNearMissBudget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryNearMissBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryNearMissBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryNearMissBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PrimaryNearMissBudgetRatioOffsetAdjuster(var weight: Double) : PrimaryNearMissBudgetModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PrimaryNearMissBudget, value: Double): Double = value + item.ratio * weight
}

/** Runs the primary near miss budget steps in order and keeps a trace of each. */
data class PrimaryNearMissBudgetEngine(
    var steps: MutableList<PrimaryNearMissBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryNearMissBudget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryNearMissBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryNearMissBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryNearMissBudgetEngine(): PrimaryNearMissBudgetEngine {
    return PrimaryNearMissBudgetEngine(
        steps = mutableListOf<PrimaryNearMissBudgetModifier>(
            CapPrimaryNearMissBudgetAdjuster(11890.0),
            CategoryPrimaryNearMissBudgetAdjuster(
                mutableMapOf<String, Double>("south" to 1.39, "retail" to 0.84),
            ),
            MinimumPrimaryNearMissBudgetAdjuster(21.6),
            TieredPrimaryNearMissBudgetAdjuster(
                mutableListOf<Double>(179.0, 6597.0, 7262.0, 9049.0),
                mutableListOf<Double>(0.96, 1.01, 0.99, 0.93),
            ),
            PrimaryNearMissBudgetRatioOffsetAdjuster(1.76),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 78096.1,
    )
}
