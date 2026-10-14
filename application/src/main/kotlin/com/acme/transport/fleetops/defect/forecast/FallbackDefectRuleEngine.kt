package com.acme.transport.fleetops.defect.forecast

/** One step of the fallback defect rule calculation. */
interface FallbackDefectRuleStep {
    fun describe(): String
    fun adjust(item: FallbackDefectRule, value: Double): Double
}

data class MinimumFallbackDefectRuleAdjuster(var minimum: Double) : FallbackDefectRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDefectRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackDefectRuleAdjuster(var cap: Double) : FallbackDefectRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDefectRule, value: Double): Double = minOf(value, cap)
}

data class TitleFallbackDefectRuleAdjuster(var factors: MutableMap<String, Double>) : FallbackDefectRuleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: FallbackDefectRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback defect rule steps in order and keeps a trace of each. */
data class FallbackDefectRuleEngine(
    var steps: MutableList<FallbackDefectRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDefectRule): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDefectRuleStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDefectRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDefectRuleEngine(): FallbackDefectRuleEngine {
    return FallbackDefectRuleEngine(
        steps = mutableListOf<FallbackDefectRuleStep>(
            MinimumFallbackDefectRuleAdjuster(43.2),
            CapFallbackDefectRuleAdjuster(26602.0),
            TitleFallbackDefectRuleAdjuster(mutableMapOf<String, Double>("online" to 1.23)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 82176.7,
    )
}
