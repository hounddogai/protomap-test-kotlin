package com.acme.transport.crewing.availability.rule

/** One step of the availability rule calculation. */
interface AvailabilityRuleModifier {
    fun describe(): String
    fun adjust(item: AvailabilityRule, value: Double): Double
}

data class PercentAvailabilityRuleAdjuster(var percent: Double) : AvailabilityRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAvailabilityRuleAdjuster(var minimum: Double) : AvailabilityRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotAvailabilityRuleAdjuster(var factors: MutableMap<String, Double>) : AvailabilityRuleModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: AvailabilityRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the availability rule steps in order and keeps a trace of each. */
data class AvailabilityRuleEngine(
    var steps: MutableList<AvailabilityRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilityRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilityRuleEngine(): AvailabilityRuleEngine {
    return AvailabilityRuleEngine(
        steps = mutableListOf<AvailabilityRuleModifier>(
            PercentAvailabilityRuleAdjuster(0.3),
            MinimumAvailabilityRuleAdjuster(22.9),
            DepotAvailabilityRuleAdjuster(
                mutableMapOf<String, Double>("west" to 1.27, "north" to 1.34, "retail" to 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 75180.5,
    )
}
