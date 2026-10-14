package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual service plan quota calculation. */
interface ManualServicePlanQuotaAdjuster {
    fun describe(): String
    fun adjust(item: ManualServicePlanQuota, value: Double): Double
}

data class RoundingManualServicePlanQuotaAdjuster(var step: Double) : ManualServicePlanQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualServicePlanQuota, value: Double): Double = roundTo(value, step)
}

data class TieredManualServicePlanQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualServicePlanQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualServicePlanQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualServicePlanQuotaAdjuster(var minimum: Double) : ManualServicePlanQuotaAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualServicePlanQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualServicePlanQuotaThresholdOffsetAdjuster(var weight: Double) : ManualServicePlanQuotaAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ManualServicePlanQuota, value: Double): Double = value + item.threshold * weight
}

data class FlaggedManualServicePlanQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualServicePlanQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualServicePlanQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual service plan quota steps in order and keeps a trace of each. */
data class ManualServicePlanQuotaEngine(
    var steps: MutableList<ManualServicePlanQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualServicePlanQuota): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualServicePlanQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualServicePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualServicePlanQuotaEngine(flags: Registry): ManualServicePlanQuotaEngine {
    return ManualServicePlanQuotaEngine(
        steps = mutableListOf<ManualServicePlanQuotaAdjuster>(
            RoundingManualServicePlanQuotaAdjuster(0.1),
            TieredManualServicePlanQuotaAdjuster(
                mutableListOf<Double>(635.0, 1031.0, 1824.0),
                mutableListOf<Double>(0.82, 1.05, 0.98),
            ),
            MinimumManualServicePlanQuotaAdjuster(40.4),
            ManualServicePlanQuotaThresholdOffsetAdjuster(0.21),
            FlaggedManualServicePlanQuotaAdjuster(flags, "manual-service-plan-quota-scheduled", -8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 27368.7,
    )
}
