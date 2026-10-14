package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment notice calculation. */
interface AppointmentNoticeModifier {
    fun describe(): String
    fun adjust(item: AppointmentNotice, value: Double): Double
}

data class CapAppointmentNoticeAdjuster(var cap: Double) : AppointmentNoticeModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AppointmentNotice, value: Double): Double = minOf(value, cap)
}

data class AppointmentNoticeQuantityOffsetAdjuster(var weight: Double) : AppointmentNoticeModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AppointmentNotice, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class MinimumAppointmentNoticeAdjuster(var minimum: Double) : AppointmentNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleAppointmentNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : AppointmentNoticeModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: AppointmentNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAppointmentNoticeAdjuster(var percent: Double) : AppointmentNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the appointment notice steps in order and keeps a trace of each. */
data class AppointmentNoticeEngine(
    var steps: MutableList<AppointmentNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentNoticeEngine(): AppointmentNoticeEngine {
    return AppointmentNoticeEngine(
        steps = mutableListOf<AppointmentNoticeModifier>(
            CapAppointmentNoticeAdjuster(14876.0),
            AppointmentNoticeQuantityOffsetAdjuster(0.34),
            MinimumAppointmentNoticeAdjuster(28.3),
            LocaleAppointmentNoticeAdjuster(
                mutableMapOf<String, Double>("east" to 0.96, "online" to 1.14, "west" to 0.7),
            ),
            PercentAppointmentNoticeAdjuster(-18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 80328.6,
    )
}
