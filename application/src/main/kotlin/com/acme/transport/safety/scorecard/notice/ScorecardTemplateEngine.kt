package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scorecard template calculation. */
interface ScorecardTemplateFactor {
    fun describe(): String
    fun adjust(item: ScorecardTemplate, value: Double): Double
}

data class RoundingScorecardTemplateAdjuster(var step: Double) : ScorecardTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScorecardTemplate, value: Double): Double = roundTo(value, step)
}

data class TitleScorecardTemplateAdjuster(var factors: MutableMap<String, Double>) : ScorecardTemplateFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ScorecardTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScorecardTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScorecardTemplateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScorecardTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScorecardTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScorecardTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScorecardTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScorecardTemplateAdjuster(var percent: Double) : ScorecardTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScorecardTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScorecardTemplatePriorityOffsetAdjuster(var weight: Double) : ScorecardTemplateFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: ScorecardTemplate, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the scorecard template steps in order and keeps a trace of each. */
data class ScorecardTemplateEngine(
    var steps: MutableList<ScorecardTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScorecardTemplate): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScorecardTemplateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScorecardTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScorecardTemplateEngine(flags: Registry): ScorecardTemplateEngine {
    return ScorecardTemplateEngine(
        steps = mutableListOf<ScorecardTemplateFactor>(
            RoundingScorecardTemplateAdjuster(5.0),
            TitleScorecardTemplateAdjuster(mutableMapOf<String, Double>("online" to 0.94, "retail" to 0.71)),
            TieredScorecardTemplateAdjuster(
                mutableListOf<Double>(1427.0, 5487.0),
                mutableListOf<Double>(1.04, 0.82),
            ),
            FlaggedScorecardTemplateAdjuster(flags, "scorecard-template-adaptive", 1.4),
            PercentScorecardTemplateAdjuster(-7.4),
            ScorecardTemplatePriorityOffsetAdjuster(1.33),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 46295.3,
    )
}
