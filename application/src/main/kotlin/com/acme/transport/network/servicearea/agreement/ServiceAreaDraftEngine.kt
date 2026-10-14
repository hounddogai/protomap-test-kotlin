package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.flags.Registry

/** One step of the service area draft calculation. */
interface ServiceAreaDraftAdjuster {
    fun describe(): String
    fun adjust(item: ServiceAreaDraft, value: Double): Double
}

data class MinimumServiceAreaDraftAdjuster(var minimum: Double) : ServiceAreaDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServiceAreaDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentServiceAreaDraftAdjuster(var percent: Double) : ServiceAreaDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ServiceAreaDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedServiceAreaDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaDraftAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service area draft steps in order and keeps a trace of each. */
data class ServiceAreaDraftEngine(
    var steps: MutableList<ServiceAreaDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ServiceAreaDraft): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServiceAreaDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ServiceAreaDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultServiceAreaDraftEngine(flags: Registry): ServiceAreaDraftEngine {
    return ServiceAreaDraftEngine(
        steps = mutableListOf<ServiceAreaDraftAdjuster>(
            MinimumServiceAreaDraftAdjuster(38.0),
            PercentServiceAreaDraftAdjuster(-21.2),
            FlaggedServiceAreaDraftAdjuster(flags, "service-area-draft-seasonal", 1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 68180.3,
    )
}
