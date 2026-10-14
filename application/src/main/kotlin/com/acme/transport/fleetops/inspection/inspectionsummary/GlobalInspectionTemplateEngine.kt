package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.flags.Registry

/** One step of the global inspection template calculation. */
interface GlobalInspectionTemplateAdjuster {
    fun describe(): String
    fun adjust(item: GlobalInspectionTemplate, value: Double): Double
}

data class FlaggedGlobalInspectionTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CountryGlobalInspectionTemplateAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalInspectionTemplateAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalInspectionTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalInspectionTemplateFloorValueOffsetAdjuster(
    var weight: Double,
) : GlobalInspectionTemplateAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: GlobalInspectionTemplate, value: Double): Double = value + item.floorValue * weight
}

data class TieredGlobalInspectionTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalInspectionTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalInspectionTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalInspectionTemplateAdjuster(var minimum: Double) : GlobalInspectionTemplateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalInspectionTemplate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global inspection template steps in order and keeps a trace of each. */
data class GlobalInspectionTemplateEngine(
    var steps: MutableList<GlobalInspectionTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalInspectionTemplate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalInspectionTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalInspectionTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalInspectionTemplateEngine(flags: Registry): GlobalInspectionTemplateEngine {
    return GlobalInspectionTemplateEngine(
        steps = mutableListOf<GlobalInspectionTemplateAdjuster>(
            FlaggedGlobalInspectionTemplateAdjuster(flags, "global-inspection-template-strict", 5.4),
            CountryGlobalInspectionTemplateAdjuster(mutableMapOf<String, Double>("west" to 0.75)),
            GlobalInspectionTemplateFloorValueOffsetAdjuster(0.18),
            TieredGlobalInspectionTemplateAdjuster(
                mutableListOf<Double>(3027.0, 4138.0),
                mutableListOf<Double>(1.12, 1.08),
            ),
            MinimumGlobalInspectionTemplateAdjuster(6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 20215.3,
    )
}
