package com.acme.transport.terminal.traileryard.profile

/** One step of the global trailer yard template calculation. */
interface GlobalTrailerYardTemplateStep {
    fun describe(): String
    fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double
}

data class PercentGlobalTrailerYardTemplateAdjuster(var percent: Double) : GlobalTrailerYardTemplateStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapGlobalTrailerYardTemplateAdjuster(var cap: Double) : GlobalTrailerYardTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double = minOf(value, cap)
}

data class GlobalTrailerYardTemplateAttemptsOffsetAdjuster(var weight: Double) : GlobalTrailerYardTemplateStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class TitleGlobalTrailerYardTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTrailerYardTemplateStep {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalTrailerYardTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTrailerYardTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTrailerYardTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global trailer yard template steps in order and keeps a trace of each. */
data class GlobalTrailerYardTemplateEngine(
    var steps: MutableList<GlobalTrailerYardTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalTrailerYardTemplate): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalTrailerYardTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalTrailerYardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalTrailerYardTemplateEngine(): GlobalTrailerYardTemplateEngine {
    return GlobalTrailerYardTemplateEngine(
        steps = mutableListOf<GlobalTrailerYardTemplateStep>(
            PercentGlobalTrailerYardTemplateAdjuster(-5.8),
            CapGlobalTrailerYardTemplateAdjuster(16980.0),
            GlobalTrailerYardTemplateAttemptsOffsetAdjuster(0.88),
            TitleGlobalTrailerYardTemplateAdjuster(
                mutableMapOf<String, Double>("online" to 0.75, "north" to 1.21, "south" to 0.99),
            ),
            TieredGlobalTrailerYardTemplateAdjuster(
                mutableListOf<Double>(424.0, 4002.0),
                mutableListOf<Double>(0.94, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 63450.4,
    )
}
