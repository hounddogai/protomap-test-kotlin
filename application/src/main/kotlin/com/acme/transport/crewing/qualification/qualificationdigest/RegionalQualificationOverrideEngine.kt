package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.flags.Registry

/** One step of the regional qualification override calculation. */
interface RegionalQualificationOverrideModifier {
    fun describe(): String
    fun adjust(item: RegionalQualificationOverride, value: Double): Double
}

data class LabelRegionalQualificationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationOverrideModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalQualificationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalQualificationOverrideAdjuster(
    var percent: Double,
) : RegionalQualificationOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalQualificationOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalQualificationOverrideCostOffsetAdjuster(
    var weight: Double,
) : RegionalQualificationOverrideModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: RegionalQualificationOverride, value: Double): Double = value + item.cost * weight
}

data class FlaggedRegionalQualificationOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalQualificationOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalQualificationOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional qualification override steps in order and keeps a trace of each. */
data class RegionalQualificationOverrideEngine(
    var steps: MutableList<RegionalQualificationOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalQualificationOverride): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalQualificationOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalQualificationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalQualificationOverrideEngine(flags: Registry): RegionalQualificationOverrideEngine {
    return RegionalQualificationOverrideEngine(
        steps = mutableListOf<RegionalQualificationOverrideModifier>(
            LabelRegionalQualificationOverrideAdjuster(mutableMapOf<String, Double>("online" to 0.91)),
            PercentRegionalQualificationOverrideAdjuster(-22.7),
            RegionalQualificationOverrideCostOffsetAdjuster(0.2),
            FlaggedRegionalQualificationOverrideAdjuster(flags, "regional-qualification-override-rolling", 3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 48324.1,
    )
}
