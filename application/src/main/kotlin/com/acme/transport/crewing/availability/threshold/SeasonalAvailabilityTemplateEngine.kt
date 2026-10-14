package com.acme.transport.crewing.availability.threshold

/** One step of the seasonal availability template calculation. */
interface SeasonalAvailabilityTemplateFactor {
    fun describe(): String
    fun adjust(item: SeasonalAvailabilityTemplate, value: Double): Double
}

data class SeasonalAvailabilityTemplateEtaMinutesOffsetAdjuster(
    var weight: Double,
) : SeasonalAvailabilityTemplateFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: SeasonalAvailabilityTemplate, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class PercentSeasonalAvailabilityTemplateAdjuster(
    var percent: Double,
) : SeasonalAvailabilityTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAvailabilityTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalAvailabilityTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAvailabilityTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAvailabilityTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal availability template steps in order and keeps a trace of each. */
data class SeasonalAvailabilityTemplateEngine(
    var steps: MutableList<SeasonalAvailabilityTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalAvailabilityTemplate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalAvailabilityTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalAvailabilityTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalAvailabilityTemplateEngine(): SeasonalAvailabilityTemplateEngine {
    return SeasonalAvailabilityTemplateEngine(
        steps = mutableListOf<SeasonalAvailabilityTemplateFactor>(
            SeasonalAvailabilityTemplateEtaMinutesOffsetAdjuster(0.26),
            PercentSeasonalAvailabilityTemplateAdjuster(-24.0),
            TieredSeasonalAvailabilityTemplateAdjuster(
                mutableListOf<Double>(1449.0, 9848.0),
                mutableListOf<Double>(1.1, 1.2),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 9343.8,
    )
}
