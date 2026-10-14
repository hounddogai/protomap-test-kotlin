package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default carbon report milestone calculation. */
interface DefaultCarbonReportMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: DefaultCarbonReportMilestone, value: Double): Double
}

data class MinimumDefaultCarbonReportMilestoneAdjuster(
    var minimum: Double,
) : DefaultCarbonReportMilestoneAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCarbonReportMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultCarbonReportMilestoneAdjuster(
    var step: Double,
) : DefaultCarbonReportMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCarbonReportMilestone, value: Double): Double = roundTo(value, step)
}

data class DefaultCarbonReportMilestoneGraceDaysOffsetAdjuster(
    var weight: Double,
) : DefaultCarbonReportMilestoneAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DefaultCarbonReportMilestone, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class FlaggedDefaultCarbonReportMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCarbonReportMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCarbonReportMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default carbon report milestone steps in order and keeps a trace of each. */
data class DefaultCarbonReportMilestoneEngine(
    var steps: MutableList<DefaultCarbonReportMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultCarbonReportMilestone): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultCarbonReportMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultCarbonReportMilestoneEngine(flags: Registry): DefaultCarbonReportMilestoneEngine {
    return DefaultCarbonReportMilestoneEngine(
        steps = mutableListOf<DefaultCarbonReportMilestoneAdjuster>(
            MinimumDefaultCarbonReportMilestoneAdjuster(27.6),
            RoundingDefaultCarbonReportMilestoneAdjuster(0.05),
            DefaultCarbonReportMilestoneGraceDaysOffsetAdjuster(0.3),
            FlaggedDefaultCarbonReportMilestoneAdjuster(flags, "default-carbon-report-milestone-adaptive", -5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 58869.7,
    )
}
