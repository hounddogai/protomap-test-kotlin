package com.acme.transport.fleetops.serviceplan.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the service plan draft calculation. */
interface ServicePlanDraftFactor {
    fun describe(): String
    fun adjust(item: ServicePlanDraft, value: Double): Double
}

data class FlaggedServicePlanDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ServicePlanDraftUpdatedAtOffsetAdjuster(var weight: Double) : ServicePlanDraftFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ServicePlanDraft, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class SegmentServicePlanDraftAdjuster(var factors: MutableMap<String, Double>) : ServicePlanDraftFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: ServicePlanDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the service plan draft steps in order and keeps a trace of each. */
data class ServicePlanDraftEngine(
    var steps: MutableList<ServicePlanDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanDraft): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanDraftEngine(flags: Registry): ServicePlanDraftEngine {
    return ServicePlanDraftEngine(
        steps = mutableListOf<ServicePlanDraftFactor>(
            FlaggedServicePlanDraftAdjuster(flags, "service-plan-draft-seasonal", -2.9),
            ServicePlanDraftUpdatedAtOffsetAdjuster(0.25),
            SegmentServicePlanDraftAdjuster(
                mutableMapOf<String, Double>("north" to 1.01, "west" to 0.89, "central" to 1.37),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 93594.3,
    )
}
