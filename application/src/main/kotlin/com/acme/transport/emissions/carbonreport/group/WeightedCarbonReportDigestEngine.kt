package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.flags.Registry

/** One step of the weighted carbon report digest calculation. */
interface WeightedCarbonReportDigestAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportDigest, value: Double): Double
}

data class FlaggedWeightedCarbonReportDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedCarbonReportDigestAdjuster(var percent: Double) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitWeightedCarbonReportDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedCarbonReportDigestAdjuster(var cap: Double) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double = minOf(value, cap)
}

data class WeightedCarbonReportDigestUpdatedAtOffsetAdjuster(
    var weight: Double,
) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class MinimumWeightedCarbonReportDigestAdjuster(var minimum: Double) : WeightedCarbonReportDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCarbonReportDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted carbon report digest steps in order and keeps a trace of each. */
data class WeightedCarbonReportDigestEngine(
    var steps: MutableList<WeightedCarbonReportDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCarbonReportDigest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCarbonReportDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCarbonReportDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportDigestEngine(flags: Registry): WeightedCarbonReportDigestEngine {
    return WeightedCarbonReportDigestEngine(
        steps = mutableListOf<WeightedCarbonReportDigestAdjuster>(
            FlaggedWeightedCarbonReportDigestAdjuster(flags, "weighted-carbon-report-digest-manual", 5.3),
            PercentWeightedCarbonReportDigestAdjuster(19.5),
            UnitWeightedCarbonReportDigestAdjuster(
                mutableMapOf<String, Double>("south" to 1.14, "retail" to 0.83, "central" to 0.87),
            ),
            CapWeightedCarbonReportDigestAdjuster(39934.0),
            WeightedCarbonReportDigestUpdatedAtOffsetAdjuster(0.13),
            MinimumWeightedCarbonReportDigestAdjuster(48.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 97652.0,
    )
}
