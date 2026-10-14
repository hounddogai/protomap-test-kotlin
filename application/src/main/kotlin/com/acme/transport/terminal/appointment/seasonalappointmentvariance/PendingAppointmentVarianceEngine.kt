package com.acme.transport.terminal.appointment.seasonalappointmentvariance

/** One step of the pending appointment variance calculation. */
interface PendingAppointmentVarianceStep {
    fun describe(): String
    fun adjust(item: PendingAppointmentVariance, value: Double): Double
}

data class CapPendingAppointmentVarianceAdjuster(var cap: Double) : PendingAppointmentVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingAppointmentVariance, value: Double): Double = minOf(value, cap)
}

data class PendingAppointmentVarianceQuantityOffsetAdjuster(
    var weight: Double,
) : PendingAppointmentVarianceStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: PendingAppointmentVariance, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class CategoryPendingAppointmentVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingAppointmentVarianceStep {
    override fun describe(): String = "by category"

    override fun adjust(item: PendingAppointmentVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending appointment variance steps in order and keeps a trace of each. */
data class PendingAppointmentVarianceEngine(
    var steps: MutableList<PendingAppointmentVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingAppointmentVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingAppointmentVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingAppointmentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingAppointmentVarianceEngine(): PendingAppointmentVarianceEngine {
    return PendingAppointmentVarianceEngine(
        steps = mutableListOf<PendingAppointmentVarianceStep>(
            CapPendingAppointmentVarianceAdjuster(44718.0),
            PendingAppointmentVarianceQuantityOffsetAdjuster(0.52),
            CategoryPendingAppointmentVarianceAdjuster(mutableMapOf<String, Double>("west" to 1.05)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 61696.7,
    )
}
