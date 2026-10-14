package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.numbers.roundTo

/** One step of the default audit schedule calculation. */
interface DefaultAuditScheduleFactor {
    fun describe(): String
    fun adjust(item: DefaultAuditSchedule, value: Double): Double
}

data class RoundingDefaultAuditScheduleAdjuster(var step: Double) : DefaultAuditScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAuditSchedule, value: Double): Double = roundTo(value, step)
}

data class NameDefaultAuditScheduleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAuditScheduleFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultAuditSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultAuditScheduleAdjuster(var minimum: Double) : DefaultAuditScheduleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultAuditSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default audit schedule steps in order and keeps a trace of each. */
data class DefaultAuditScheduleEngine(
    var steps: MutableList<DefaultAuditScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultAuditSchedule): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAuditScheduleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAuditSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultAuditScheduleEngine(): DefaultAuditScheduleEngine {
    return DefaultAuditScheduleEngine(
        steps = mutableListOf<DefaultAuditScheduleFactor>(
            RoundingDefaultAuditScheduleAdjuster(0.5),
            NameDefaultAuditScheduleAdjuster(
                mutableMapOf<String, Double>("online" to 0.91, "retail" to 1.08, "east" to 0.9),
            ),
            MinimumDefaultAuditScheduleAdjuster(35.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 78154.8,
    )
}
