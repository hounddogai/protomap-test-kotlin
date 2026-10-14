package com.acme.transport.linehaul.laneplan.request

/** One step of the legacy lane plan quota calculation. */
interface LegacyLanePlanQuotaFactor {
    fun describe(): String
    fun adjust(item: LegacyLanePlanQuota, value: Double): Double
}

data class LegacyLanePlanQuotaDiscountOffsetAdjuster(var weight: Double) : LegacyLanePlanQuotaFactor {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: LegacyLanePlanQuota, value: Double): Double = value + item.discount * weight
}

data class SegmentLegacyLanePlanQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyLanePlanQuotaFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: LegacyLanePlanQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyLanePlanQuotaAdjuster(var minimum: Double) : LegacyLanePlanQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLanePlanQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy lane plan quota steps in order and keeps a trace of each. */
data class LegacyLanePlanQuotaEngine(
    var steps: MutableList<LegacyLanePlanQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyLanePlanQuota): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLanePlanQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLanePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyLanePlanQuotaEngine(): LegacyLanePlanQuotaEngine {
    return LegacyLanePlanQuotaEngine(
        steps = mutableListOf<LegacyLanePlanQuotaFactor>(
            LegacyLanePlanQuotaDiscountOffsetAdjuster(1.63),
            SegmentLegacyLanePlanQuotaAdjuster(
                mutableMapOf<String, Double>("retail" to 0.81, "central" to 1.36, "online" to 0.96),
            ),
            MinimumLegacyLanePlanQuotaAdjuster(25.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 25545.4,
    )
}
