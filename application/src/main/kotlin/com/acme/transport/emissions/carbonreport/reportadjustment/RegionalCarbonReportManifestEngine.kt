package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.kit.flags.Registry

/** One step of the regional carbon report manifest calculation. */
interface RegionalCarbonReportManifestModifier {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportManifest, value: Double): Double
}

data class FlaggedRegionalCarbonReportManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCarbonReportManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalCarbonReportManifestAdjuster(var cap: Double) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double = minOf(value, cap)
}

data class RegionalCarbonReportManifestVersionOffsetAdjuster(
    var weight: Double,
) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class SourceRegionalCarbonReportManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalCarbonReportManifestAdjuster(
    var minimum: Double,
) : RegionalCarbonReportManifestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCarbonReportManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional carbon report manifest steps in order and keeps a trace of each. */
data class RegionalCarbonReportManifestEngine(
    var steps: MutableList<RegionalCarbonReportManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCarbonReportManifest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCarbonReportManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportManifestEngine(flags: Registry): RegionalCarbonReportManifestEngine {
    return RegionalCarbonReportManifestEngine(
        steps = mutableListOf<RegionalCarbonReportManifestModifier>(
            FlaggedRegionalCarbonReportManifestAdjuster(flags, "regional-carbon-report-manifest-batched", 6.9),
            TieredRegionalCarbonReportManifestAdjuster(
                mutableListOf<Double>(6902.0, 7552.0, 9310.0),
                mutableListOf<Double>(1.13, 0.95, 1.17),
            ),
            CapRegionalCarbonReportManifestAdjuster(33740.0),
            RegionalCarbonReportManifestVersionOffsetAdjuster(1.09),
            SourceRegionalCarbonReportManifestAdjuster(
                mutableMapOf<String, Double>("north" to 0.76, "retail" to 1.16),
            ),
            MinimumRegionalCarbonReportManifestAdjuster(47.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 83140.6,
    )
}
