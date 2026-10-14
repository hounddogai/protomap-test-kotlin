package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal inspection digest calculation. */
interface SeasonalInspectionDigestAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalInspectionDigest, value: Double): Double
}

data class SeasonalInspectionDigestRevisionOffsetAdjuster(
    var weight: Double,
) : SeasonalInspectionDigestAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: SeasonalInspectionDigest, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class PercentSeasonalInspectionDigestAdjuster(var percent: Double) : SeasonalInspectionDigestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalInspectionDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSeasonalInspectionDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalInspectionDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalInspectionDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal inspection digest steps in order and keeps a trace of each. */
data class SeasonalInspectionDigestEngine(
    var steps: MutableList<SeasonalInspectionDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalInspectionDigest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalInspectionDigestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalInspectionDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalInspectionDigestEngine(flags: Registry): SeasonalInspectionDigestEngine {
    return SeasonalInspectionDigestEngine(
        steps = mutableListOf<SeasonalInspectionDigestAdjuster>(
            SeasonalInspectionDigestRevisionOffsetAdjuster(0.73),
            PercentSeasonalInspectionDigestAdjuster(-23.5),
            FlaggedSeasonalInspectionDigestAdjuster(flags, "seasonal-inspection-digest-legacy", 4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 37752.1,
    )
}
