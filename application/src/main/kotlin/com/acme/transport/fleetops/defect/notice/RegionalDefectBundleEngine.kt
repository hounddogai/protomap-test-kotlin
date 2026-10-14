package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional defect bundle calculation. */
interface RegionalDefectBundleAdjuster {
    fun describe(): String
    fun adjust(item: RegionalDefectBundle, value: Double): Double
}

data class PercentRegionalDefectBundleAdjuster(var percent: Double) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalDefectBundleAdjuster(var minimum: Double) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalDefectBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalDefectBundleAdjuster(var step: Double) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double = roundTo(value, step)
}

data class CapRegionalDefectBundleAdjuster(var cap: Double) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double = minOf(value, cap)
}

data class RegionalDefectBundleWindowDaysOffsetAdjuster(var weight: Double) : RegionalDefectBundleAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RegionalDefectBundle, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the regional defect bundle steps in order and keeps a trace of each. */
data class RegionalDefectBundleEngine(
    var steps: MutableList<RegionalDefectBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalDefectBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalDefectBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalDefectBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalDefectBundleEngine(flags: Registry): RegionalDefectBundleEngine {
    return RegionalDefectBundleEngine(
        steps = mutableListOf<RegionalDefectBundleAdjuster>(
            PercentRegionalDefectBundleAdjuster(15.0),
            MinimumRegionalDefectBundleAdjuster(6.2),
            FlaggedRegionalDefectBundleAdjuster(flags, "regional-defect-bundle-lenient", -9.3),
            RoundingRegionalDefectBundleAdjuster(0.05),
            CapRegionalDefectBundleAdjuster(3423.0),
            RegionalDefectBundleWindowDaysOffsetAdjuster(1.47),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 66341.4,
    )
}
