package com.acme.transport.terminal.appointment.summary

/** One step of the appointment summary calculation. */
interface AppointmentSummaryModifier {
    fun describe(): String
    fun adjust(item: AppointmentSummary, value: Double): Double
}

data class CapAppointmentSummaryAdjuster(var cap: Double) : AppointmentSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentSummary, value: Double): Double = minOf(value, cap)
}

data class AppointmentSummaryRevisionOffsetAdjuster(var weight: Double) : AppointmentSummaryModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: AppointmentSummary, value: Double): Double = value + item.revision.toDouble() * weight
}

data class TieredAppointmentSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AppointmentSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AppointmentSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAppointmentSummaryAdjuster(var minimum: Double) : AppointmentSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAppointmentSummaryAdjuster(var percent: Double) : AppointmentSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelAppointmentSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentSummaryModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: AppointmentSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the appointment summary steps in order and keeps a trace of each. */
data class AppointmentSummaryEngine(
    var steps: MutableList<AppointmentSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AppointmentSummary): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AppointmentSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AppointmentSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAppointmentSummaryEngine(): AppointmentSummaryEngine {
    return AppointmentSummaryEngine(
        steps = mutableListOf<AppointmentSummaryModifier>(
            CapAppointmentSummaryAdjuster(39105.0),
            AppointmentSummaryRevisionOffsetAdjuster(0.23),
            TieredAppointmentSummaryAdjuster(
                mutableListOf<Double>(351.0, 3640.0, 9267.0, 9480.0),
                mutableListOf<Double>(0.94, 0.9, 0.95, 1.12),
            ),
            MinimumAppointmentSummaryAdjuster(9.4),
            PercentAppointmentSummaryAdjuster(22.1),
            ChannelAppointmentSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.38)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 28480.0,
    )
}
