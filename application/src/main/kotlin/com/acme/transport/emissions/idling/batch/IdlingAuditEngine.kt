package com.acme.transport.emissions.idling.batch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling audit calculation. */
interface IdlingAuditAdjuster {
    fun describe(): String
    fun adjust(item: IdlingAudit, value: Double): Double
}

data class TieredIdlingAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IdlingAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IdlingAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentIdlingAuditAdjuster(var percent: Double) : IdlingAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumIdlingAuditAdjuster(var minimum: Double) : IdlingAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapIdlingAuditAdjuster(var cap: Double) : IdlingAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the idling audit steps in order and keeps a trace of each. */
data class IdlingAuditEngine(
    var steps: MutableList<IdlingAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingAudit): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingAuditEngine(): IdlingAuditEngine {
    return IdlingAuditEngine(
        steps = mutableListOf<IdlingAuditAdjuster>(
            TieredIdlingAuditAdjuster(mutableListOf<Double>(1938.0, 8673.0), mutableListOf<Double>(0.93, 0.99)),
            PercentIdlingAuditAdjuster(9.1),
            MinimumIdlingAuditAdjuster(13.1),
            CapIdlingAuditAdjuster(23265.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 89381.1,
    )
}
