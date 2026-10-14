package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy roster template calculation. */
interface LegacyRosterTemplateFactor {
    fun describe(): String
    fun adjust(item: LegacyRosterTemplate, value: Double): Double
}

data class CapLegacyRosterTemplateAdjuster(var cap: Double) : LegacyRosterTemplateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double = minOf(value, cap)
}

data class TitleLegacyRosterTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyRosterTemplateFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyRosterTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyRosterTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyRosterTemplateAdjuster(var percent: Double) : LegacyRosterTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyRosterTemplateAdjuster(var minimum: Double) : LegacyRosterTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyRosterTemplateGraceDaysOffsetAdjuster(var weight: Double) : LegacyRosterTemplateFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LegacyRosterTemplate, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the legacy roster template steps in order and keeps a trace of each. */
data class LegacyRosterTemplateEngine(
    var steps: MutableList<LegacyRosterTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyRosterTemplate): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyRosterTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyRosterTemplateEngine(flags: Registry): LegacyRosterTemplateEngine {
    return LegacyRosterTemplateEngine(
        steps = mutableListOf<LegacyRosterTemplateFactor>(
            CapLegacyRosterTemplateAdjuster(29854.0),
            TitleLegacyRosterTemplateAdjuster(mutableMapOf<String, Double>("online" to 1.24, "central" to 0.94)),
            FlaggedLegacyRosterTemplateAdjuster(flags, "legacy-roster-template-fallback", 2.0),
            PercentLegacyRosterTemplateAdjuster(-23.2),
            MinimumLegacyRosterTemplateAdjuster(39.2),
            LegacyRosterTemplateGraceDaysOffsetAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 5186.3,
    )
}
