package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled service plan tier calculation. */
interface ScheduledServicePlanTierFactor {
    fun describe(): String
    fun adjust(item: ScheduledServicePlanTier, value: Double): Double
}

data class DepotScheduledServicePlanTierAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledServicePlanTierFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ScheduledServicePlanTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledServicePlanTierAdjuster(var minimum: Double) : ScheduledServicePlanTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledServicePlanTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledServicePlanTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledServicePlanTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledServicePlanTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledServicePlanTierAdjuster(var cap: Double) : ScheduledServicePlanTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledServicePlanTier, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled service plan tier steps in order and keeps a trace of each. */
data class ScheduledServicePlanTierEngine(
    var steps: MutableList<ScheduledServicePlanTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledServicePlanTier): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledServicePlanTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledServicePlanTierEngine(flags: Registry): ScheduledServicePlanTierEngine {
    return ScheduledServicePlanTierEngine(
        steps = mutableListOf<ScheduledServicePlanTierFactor>(
            DepotScheduledServicePlanTierAdjuster(mutableMapOf<String, Double>("south" to 0.94)),
            MinimumScheduledServicePlanTierAdjuster(12.5),
            FlaggedScheduledServicePlanTierAdjuster(flags, "scheduled-service-plan-tier-seasonal", -0.7),
            CapScheduledServicePlanTierAdjuster(35952.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 36601.2,
    )
}
