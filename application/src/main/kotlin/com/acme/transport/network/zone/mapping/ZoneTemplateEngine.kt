package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.flags.Registry

/** One step of the zone template calculation. */
interface ZoneTemplateStep {
    fun describe(): String
    fun adjust(item: ZoneTemplate, value: Double): Double
}

data class FlaggedZoneTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneTemplateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ZoneTemplatePriorityOffsetAdjuster(var weight: Double) : ZoneTemplateStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: ZoneTemplate, value: Double): Double = value + item.priority.toDouble() * weight
}

data class MinimumZoneTemplateAdjuster(var minimum: Double) : ZoneTemplateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredZoneTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the zone template steps in order and keeps a trace of each. */
data class ZoneTemplateEngine(
    var steps: MutableList<ZoneTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ZoneTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultZoneTemplateEngine(flags: Registry): ZoneTemplateEngine {
    return ZoneTemplateEngine(
        steps = mutableListOf<ZoneTemplateStep>(
            FlaggedZoneTemplateAdjuster(flags, "zone-template-batched", 5.8),
            ZoneTemplatePriorityOffsetAdjuster(0.37),
            MinimumZoneTemplateAdjuster(33.5),
            TieredZoneTemplateAdjuster(mutableListOf<Double>(2809.0, 3211.0), mutableListOf<Double>(0.94, 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 5907.4,
    )
}
