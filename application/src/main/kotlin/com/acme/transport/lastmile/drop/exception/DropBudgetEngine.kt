package com.acme.transport.lastmile.drop.exception

/** One step of the drop budget calculation. */
interface DropBudgetModifier {
    fun describe(): String
    fun adjust(item: DropBudget, value: Double): Double
}

data class TieredDropBudgetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DropBudgetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DropBudget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDropBudgetAdjuster(var percent: Double) : DropBudgetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropBudget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DropBudgetMarginOffsetAdjuster(var weight: Double) : DropBudgetModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: DropBudget, value: Double): Double = value + item.margin * weight
}

data class CountryDropBudgetAdjuster(var factors: MutableMap<String, Double>) : DropBudgetModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: DropBudget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the drop budget steps in order and keeps a trace of each. */
data class DropBudgetEngine(
    var steps: MutableList<DropBudgetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropBudget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropBudgetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DropBudget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropBudgetEngine(): DropBudgetEngine {
    return DropBudgetEngine(
        steps = mutableListOf<DropBudgetModifier>(
            TieredDropBudgetAdjuster(mutableListOf<Double>(1230.0, 1494.0), mutableListOf<Double>(0.99, 1.18)),
            PercentDropBudgetAdjuster(-22.6),
            DropBudgetMarginOffsetAdjuster(0.12),
            CountryDropBudgetAdjuster(mutableMapOf<String, Double>("south" to 1.28)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 32902.9,
    )
}
