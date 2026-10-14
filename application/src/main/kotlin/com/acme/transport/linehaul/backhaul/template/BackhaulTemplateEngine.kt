package com.acme.transport.linehaul.backhaul.template

/** One step of the backhaul template calculation. */
interface BackhaulTemplateModifier {
    fun describe(): String
    fun adjust(item: BackhaulTemplate, value: Double): Double
}

data class PercentBackhaulTemplateAdjuster(var percent: Double) : BackhaulTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameBackhaulTemplateAdjuster(var factors: MutableMap<String, Double>) : BackhaulTemplateModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: BackhaulTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumBackhaulTemplateAdjuster(var minimum: Double) : BackhaulTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredBackhaulTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulTemplateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul template steps in order and keeps a trace of each. */
data class BackhaulTemplateEngine(
    var steps: MutableList<BackhaulTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulTemplate): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulTemplateEngine(): BackhaulTemplateEngine {
    return BackhaulTemplateEngine(
        steps = mutableListOf<BackhaulTemplateModifier>(
            PercentBackhaulTemplateAdjuster(6.4),
            NameBackhaulTemplateAdjuster(mutableMapOf<String, Double>("online" to 1.25, "north" to 0.81)),
            MinimumBackhaulTemplateAdjuster(4.9),
            TieredBackhaulTemplateAdjuster(
                mutableListOf<Double>(378.0, 2469.0, 3824.0, 5032.0),
                mutableListOf<Double>(0.86, 0.99, 0.88, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 49943.5,
    )
}
