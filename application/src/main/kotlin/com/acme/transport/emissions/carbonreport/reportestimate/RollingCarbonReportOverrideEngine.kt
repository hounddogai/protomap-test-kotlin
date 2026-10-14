package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling carbon report override calculation. */
interface RollingCarbonReportOverrideModifier {
    fun describe(): String
    fun adjust(item: RollingCarbonReportOverride, value: Double): Double
}

data class CapRollingCarbonReportOverrideAdjuster(var cap: Double) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumRollingCarbonReportOverrideAdjuster(
    var minimum: Double,
) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRollingCarbonReportOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRollingCarbonReportOverrideAdjuster(
    var percent: Double,
) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingCarbonReportOverrideWindowDaysOffsetAdjuster(
    var weight: Double,
) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class FlaggedRollingCarbonReportOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCarbonReportOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCarbonReportOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling carbon report override steps in order and keeps a trace of each. */
data class RollingCarbonReportOverrideEngine(
    var steps: MutableList<RollingCarbonReportOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingCarbonReportOverride): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCarbonReportOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingCarbonReportOverrideEngine(flags: Registry): RollingCarbonReportOverrideEngine {
    return RollingCarbonReportOverrideEngine(
        steps = mutableListOf<RollingCarbonReportOverrideModifier>(
            CapRollingCarbonReportOverrideAdjuster(20140.0),
            MinimumRollingCarbonReportOverrideAdjuster(39.1),
            TieredRollingCarbonReportOverrideAdjuster(
                mutableListOf<Double>(166.0, 5684.0),
                mutableListOf<Double>(1.08, 1.06),
            ),
            PercentRollingCarbonReportOverrideAdjuster(9.2),
            RollingCarbonReportOverrideWindowDaysOffsetAdjuster(1.51),
            FlaggedRollingCarbonReportOverrideAdjuster(flags, "rolling-carbon-report-override-pending", 4.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 27562.6,
    )
}
