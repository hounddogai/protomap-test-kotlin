package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.flags.Registry

/** One step of the service area profile calculation. */
interface ServiceAreaProfileStep {
    fun describe(): String
    fun adjust(item: ServiceAreaProfile, value: Double): Double
}

data class MinimumServiceAreaProfileAdjuster(var minimum: Double) : ServiceAreaProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ServiceAreaProfileStartsAtOffsetAdjuster(var weight: Double) : ServiceAreaProfileStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: ServiceAreaProfile, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class FlaggedServiceAreaProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotServiceAreaProfileAdjuster(var factors: MutableMap<String, Double>) : ServiceAreaProfileStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: ServiceAreaProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentServiceAreaProfileAdjuster(var percent: Double) : ServiceAreaProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the service area profile steps in order and keeps a trace of each. */
data class ServiceAreaProfileEngine(
    var steps: MutableList<ServiceAreaProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaProfile): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaProfileEngine(flags: Registry): ServiceAreaProfileEngine {
    return ServiceAreaProfileEngine(
        steps = mutableListOf<ServiceAreaProfileStep>(
            MinimumServiceAreaProfileAdjuster(9.7),
            ServiceAreaProfileStartsAtOffsetAdjuster(1.46),
            FlaggedServiceAreaProfileAdjuster(flags, "service-area-profile-lenient", -1.2),
            DepotServiceAreaProfileAdjuster(mutableMapOf<String, Double>("retail" to 0.98, "central" to 0.84)),
            PercentServiceAreaProfileAdjuster(-15.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 95951.1,
    )
}
