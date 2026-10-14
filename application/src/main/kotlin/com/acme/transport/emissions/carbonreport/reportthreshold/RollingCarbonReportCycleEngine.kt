package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling carbon report cycle calculation. */
interface RollingCarbonReportCycleAdjuster {
    fun describe(): String
    fun adjust(item: RollingCarbonReportCycle, value: Double): Double
}

data class TieredRollingCarbonReportCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCarbonReportCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCarbonReportCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingCarbonReportCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingCarbonReportCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingCarbonReportCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RollingCarbonReportCycleRatioOffsetAdjuster(var weight: Double) : RollingCarbonReportCycleAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RollingCarbonReportCycle, value: Double): Double = value + item.ratio * weight
}

data class MinimumRollingCarbonReportCycleAdjuster(var minimum: Double) : RollingCarbonReportCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCarbonReportCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling carbon report cycle steps in order and keeps a trace of each. */
data class RollingCarbonReportCycleEngine(
    var steps: MutableList<RollingCarbonReportCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCarbonReportCycle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCarbonReportCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCarbonReportCycleEngine(flags: Registry): RollingCarbonReportCycleEngine {
    return RollingCarbonReportCycleEngine(
        steps = mutableListOf<RollingCarbonReportCycleAdjuster>(
            TieredRollingCarbonReportCycleAdjuster(
                mutableListOf<Double>(4541.0, 6957.0),
                mutableListOf<Double>(0.91, 1.16),
            ),
            FlaggedRollingCarbonReportCycleAdjuster(flags, "rolling-carbon-report-cycle-weighted", 4.1),
            RollingCarbonReportCycleRatioOffsetAdjuster(0.71),
            MinimumRollingCarbonReportCycleAdjuster(42.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 41436.3,
    )
}
