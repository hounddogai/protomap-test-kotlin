package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.flags.Registry

/** One step of the service plan batch calculation. */
interface ServicePlanBatchStep {
    fun describe(): String
    fun adjust(item: ServicePlanBatch, value: Double): Double
}

data class TieredServicePlanBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedServicePlanBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelServicePlanBatchAdjuster(var factors: MutableMap<String, Double>) : ServicePlanBatchStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ServicePlanBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapServicePlanBatchAdjuster(var cap: Double) : ServicePlanBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanBatch, value: Double): Double = minOf(value, cap)
}

data class PercentServicePlanBatchAdjuster(var percent: Double) : ServicePlanBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServicePlanBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service plan batch steps in order and keeps a trace of each. */
data class ServicePlanBatchEngine(
    var steps: MutableList<ServicePlanBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ServicePlanBatch): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultServicePlanBatchEngine(flags: Registry): ServicePlanBatchEngine {
    return ServicePlanBatchEngine(
        steps = mutableListOf<ServicePlanBatchStep>(
            TieredServicePlanBatchAdjuster(
                mutableListOf<Double>(6033.0, 6960.0, 7326.0),
                mutableListOf<Double>(1.15, 0.92, 1.18),
            ),
            FlaggedServicePlanBatchAdjuster(flags, "service-plan-batch-pending", -2.2),
            LabelServicePlanBatchAdjuster(mutableMapOf<String, Double>("central" to 0.7)),
            CapServicePlanBatchAdjuster(35747.0),
            PercentServicePlanBatchAdjuster(4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 31791.1,
    )
}
