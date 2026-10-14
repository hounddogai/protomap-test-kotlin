package com.acme.transport.network.node.template

/** One step of the seasonal node template calculation. */
interface SeasonalNodeTemplateFactor {
    fun describe(): String
    fun adjust(item: SeasonalNodeTemplate, value: Double): Double
}

data class CapSeasonalNodeTemplateAdjuster(var cap: Double) : SeasonalNodeTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalNodeTemplate, value: Double): Double = minOf(value, cap)
}

data class SeasonalNodeTemplateOdometerOffsetAdjuster(var weight: Double) : SeasonalNodeTemplateFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: SeasonalNodeTemplate, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class MinimumSeasonalNodeTemplateAdjuster(var minimum: Double) : SeasonalNodeTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalNodeTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalNodeTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalNodeTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalNodeTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal node template steps in order and keeps a trace of each. */
data class SeasonalNodeTemplateEngine(
    var steps: MutableList<SeasonalNodeTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalNodeTemplate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalNodeTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalNodeTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalNodeTemplateEngine(): SeasonalNodeTemplateEngine {
    return SeasonalNodeTemplateEngine(
        steps = mutableListOf<SeasonalNodeTemplateFactor>(
            CapSeasonalNodeTemplateAdjuster(993.0),
            SeasonalNodeTemplateOdometerOffsetAdjuster(1.85),
            MinimumSeasonalNodeTemplateAdjuster(25.8),
            TieredSeasonalNodeTemplateAdjuster(
                mutableListOf<Double>(1298.0, 1478.0, 3983.0),
                mutableListOf<Double>(0.91, 1.11, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 46828.2,
    )
}
