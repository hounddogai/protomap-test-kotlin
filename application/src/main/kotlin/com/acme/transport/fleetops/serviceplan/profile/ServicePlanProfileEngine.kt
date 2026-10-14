package com.acme.transport.fleetops.serviceplan.profile

import com.acme.transport.kit.flags.Registry

/** One step of the service plan profile calculation. */
interface ServicePlanProfileStep {
    fun describe(): String
    fun adjust(item: ServicePlanProfile, value: Double): Double
}

data class FlaggedServicePlanProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServicePlanProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServicePlanProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleServicePlanProfileAdjuster(var factors: MutableMap<String, Double>) : ServicePlanProfileStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ServicePlanProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ServicePlanProfileFuelLitersOffsetAdjuster(var weight: Double) : ServicePlanProfileStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: ServicePlanProfile, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the service plan profile steps in order and keeps a trace of each. */
data class ServicePlanProfileEngine(
    var steps: MutableList<ServicePlanProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanProfile): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanProfileEngine(flags: Registry): ServicePlanProfileEngine {
    return ServicePlanProfileEngine(
        steps = mutableListOf<ServicePlanProfileStep>(
            FlaggedServicePlanProfileAdjuster(flags, "service-plan-profile-capped", 5.9),
            TitleServicePlanProfileAdjuster(mutableMapOf<String, Double>("south" to 0.88)),
            ServicePlanProfileFuelLitersOffsetAdjuster(1.92),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 35511.4,
    )
}
