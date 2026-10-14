package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.flags.Registry

/** One step of the lane plan quota calculation. */
interface LanePlanQuotaFactor {
    fun describe(): String
    fun adjust(item: LanePlanQuota, value: Double): Double
}

data class MinimumLanePlanQuotaAdjuster(var minimum: Double) : LanePlanQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLanePlanQuotaAdjuster(var cap: Double) : LanePlanQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanQuota, value: Double): Double = minOf(value, cap)
}

data class FlaggedLanePlanQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameLanePlanQuotaAdjuster(var factors: MutableMap<String, Double>) : LanePlanQuotaFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LanePlanQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lane plan quota steps in order and keeps a trace of each. */
data class LanePlanQuotaEngine(
    var steps: MutableList<LanePlanQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanQuotaEngine(flags: Registry): LanePlanQuotaEngine {
    return LanePlanQuotaEngine(
        steps = mutableListOf<LanePlanQuotaFactor>(
            MinimumLanePlanQuotaAdjuster(25.1),
            CapLanePlanQuotaAdjuster(37138.0),
            FlaggedLanePlanQuotaAdjuster(flags, "lane-plan-quota-legacy", -6.5),
            NameLanePlanQuotaAdjuster(mutableMapOf<String, Double>("west" to 1.31, "east" to 1.31)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 55938.9,
    )
}
