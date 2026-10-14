package com.acme.transport.network.hub.template

/** One step of the pending hub template calculation. */
interface PendingHubTemplateModifier {
    fun describe(): String
    fun adjust(item: PendingHubTemplate, value: Double): Double
}

data class PercentPendingHubTemplateAdjuster(var percent: Double) : PendingHubTemplateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHubTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitlePendingHubTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingHubTemplateModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingHubTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingHubTemplateAdjuster(var minimum: Double) : PendingHubTemplateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHubTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending hub template steps in order and keeps a trace of each. */
data class PendingHubTemplateEngine(
    var steps: MutableList<PendingHubTemplateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingHubTemplate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingHubTemplateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingHubTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingHubTemplateEngine(): PendingHubTemplateEngine {
    return PendingHubTemplateEngine(
        steps = mutableListOf<PendingHubTemplateModifier>(
            PercentPendingHubTemplateAdjuster(-5.1),
            TitlePendingHubTemplateAdjuster(mutableMapOf<String, Double>("east" to 0.81)),
            MinimumPendingHubTemplateAdjuster(3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 79925.7,
    )
}
