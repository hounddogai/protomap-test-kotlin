package com.acme.transport.network.zone.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global zone template calculation. */
interface GlobalZoneTemplateFactor {
    fun describe(): String
    fun adjust(item: GlobalZoneTemplate, value: Double): Double
}

data class PercentGlobalZoneTemplateAdjuster(var percent: Double) : GlobalZoneTemplateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalZoneTemplateAdjuster(var minimum: Double) : GlobalZoneTemplateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalZoneTemplateAdjuster(var step: Double) : GlobalZoneTemplateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double = roundTo(value, step)
}

data class FlaggedGlobalZoneTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalZoneTemplateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleGlobalZoneTemplateAdjuster(var factors: MutableMap<String, Double>) : GlobalZoneTemplateFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalZoneTemplateLoadKgOffsetAdjuster(var weight: Double) : GlobalZoneTemplateFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: GlobalZoneTemplate, value: Double): Double = value + item.loadKg * weight
}

/** Runs the global zone template steps in order and keeps a trace of each. */
data class GlobalZoneTemplateEngine(
    var steps: MutableList<GlobalZoneTemplateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalZoneTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalZoneTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalZoneTemplateEngine(flags: Registry): GlobalZoneTemplateEngine {
    return GlobalZoneTemplateEngine(
        steps = mutableListOf<GlobalZoneTemplateFactor>(
            PercentGlobalZoneTemplateAdjuster(-24.9),
            MinimumGlobalZoneTemplateAdjuster(5.3),
            RoundingGlobalZoneTemplateAdjuster(1.0),
            FlaggedGlobalZoneTemplateAdjuster(flags, "global-zone-template-scheduled", -2.8),
            TitleGlobalZoneTemplateAdjuster(
                mutableMapOf<String, Double>("north" to 1.28, "central" to 0.7, "east" to 1.36),
            ),
            GlobalZoneTemplateLoadKgOffsetAdjuster(0.55),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 51835.6,
    )
}
