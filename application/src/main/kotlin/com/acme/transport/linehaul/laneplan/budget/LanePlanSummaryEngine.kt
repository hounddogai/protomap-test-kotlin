package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.flags.Registry

/** One step of the lane plan summary calculation. */
interface LanePlanSummaryAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanSummary, value: Double): Double
}

data class LaneLanePlanSummaryAdjuster(var factors: MutableMap<String, Double>) : LanePlanSummaryAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LanePlanSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLanePlanSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLanePlanSummaryAdjuster(var percent: Double) : LanePlanSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLanePlanSummaryAdjuster(var cap: Double) : LanePlanSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the lane plan summary steps in order and keeps a trace of each. */
data class LanePlanSummaryEngine(
    var steps: MutableList<LanePlanSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanSummary): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanSummaryEngine(flags: Registry): LanePlanSummaryEngine {
    return LanePlanSummaryEngine(
        steps = mutableListOf<LanePlanSummaryAdjuster>(
            LaneLanePlanSummaryAdjuster(mutableMapOf<String, Double>("west" to 0.81, "central" to 1.21)),
            FlaggedLanePlanSummaryAdjuster(flags, "lane-plan-summary-default", -8.5),
            PercentLanePlanSummaryAdjuster(5.5),
            CapLanePlanSummaryAdjuster(37205.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 23530.6,
    )
}
