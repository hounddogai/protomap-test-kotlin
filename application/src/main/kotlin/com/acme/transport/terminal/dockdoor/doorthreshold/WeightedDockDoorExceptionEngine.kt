package com.acme.transport.terminal.dockdoor.doorthreshold

/** One step of the weighted dock door exception calculation. */
interface WeightedDockDoorExceptionModifier {
    fun describe(): String
    fun adjust(item: WeightedDockDoorException, value: Double): Double
}

data class TieredWeightedDockDoorExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedDockDoorExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedDockDoorException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleWeightedDockDoorExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedDockDoorExceptionModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: WeightedDockDoorException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedDockDoorExceptionAdjuster(var percent: Double) : WeightedDockDoorExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedDockDoorException, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted dock door exception steps in order and keeps a trace of each. */
data class WeightedDockDoorExceptionEngine(
    var steps: MutableList<WeightedDockDoorExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedDockDoorException): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedDockDoorExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedDockDoorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedDockDoorExceptionEngine(): WeightedDockDoorExceptionEngine {
    return WeightedDockDoorExceptionEngine(
        steps = mutableListOf<WeightedDockDoorExceptionModifier>(
            TieredWeightedDockDoorExceptionAdjuster(
                mutableListOf<Double>(347.0, 5070.0, 6242.0, 9590.0),
                mutableListOf<Double>(0.95, 1.15, 0.81, 1.05),
            ),
            LocaleWeightedDockDoorExceptionAdjuster(
                mutableMapOf<String, Double>("south" to 1.17, "retail" to 1.06, "north" to 0.75),
            ),
            PercentWeightedDockDoorExceptionAdjuster(12.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 47401.0,
    )
}
