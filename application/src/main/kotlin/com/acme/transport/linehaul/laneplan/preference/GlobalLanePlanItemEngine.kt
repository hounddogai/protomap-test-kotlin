package com.acme.transport.linehaul.laneplan.preference

/** One step of the global lane plan item calculation. */
interface GlobalLanePlanItemModifier {
    fun describe(): String
    fun adjust(item: GlobalLanePlanItem, value: Double): Double
}

data class PercentGlobalLanePlanItemAdjuster(var percent: Double) : GlobalLanePlanItemModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalLanePlanItem, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleGlobalLanePlanItemAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalLanePlanItemModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalLanePlanItem, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalLanePlanItemAdjuster(var minimum: Double) : GlobalLanePlanItemModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalLanePlanItem, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global lane plan item steps in order and keeps a trace of each. */
data class GlobalLanePlanItemEngine(
    var steps: MutableList<GlobalLanePlanItemModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalLanePlanItem): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalLanePlanItemModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalLanePlanItem>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalLanePlanItemEngine(): GlobalLanePlanItemEngine {
    return GlobalLanePlanItemEngine(
        steps = mutableListOf<GlobalLanePlanItemModifier>(
            PercentGlobalLanePlanItemAdjuster(15.2),
            TitleGlobalLanePlanItemAdjuster(mutableMapOf<String, Double>("east" to 1.18, "north" to 0.81)),
            MinimumGlobalLanePlanItemAdjuster(9.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 70909.5,
    )
}
