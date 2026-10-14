package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the batched appointment notice calculation. */
interface BatchedAppointmentNoticeStep {
    fun describe(): String
    fun adjust(item: BatchedAppointmentNotice, value: Double): Double
}

data class RoundingBatchedAppointmentNoticeAdjuster(var step: Double) : BatchedAppointmentNoticeStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedAppointmentNotice, value: Double): Double = roundTo(value, step)
}

data class TieredBatchedAppointmentNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedAppointmentNoticeStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedAppointmentNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitBatchedAppointmentNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAppointmentNoticeStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: BatchedAppointmentNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedAppointmentNoticeQuantityOffsetAdjuster(var weight: Double) : BatchedAppointmentNoticeStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: BatchedAppointmentNotice, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class FlaggedBatchedAppointmentNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAppointmentNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAppointmentNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the batched appointment notice steps in order and keeps a trace of each. */
data class BatchedAppointmentNoticeEngine(
    var steps: MutableList<BatchedAppointmentNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BatchedAppointmentNotice): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAppointmentNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAppointmentNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBatchedAppointmentNoticeEngine(flags: Registry): BatchedAppointmentNoticeEngine {
    return BatchedAppointmentNoticeEngine(
        steps = mutableListOf<BatchedAppointmentNoticeStep>(
            RoundingBatchedAppointmentNoticeAdjuster(0.25),
            TieredBatchedAppointmentNoticeAdjuster(
                mutableListOf<Double>(3699.0, 4545.0, 6313.0, 8298.0),
                mutableListOf<Double>(1.01, 0.86, 0.97, 0.94),
            ),
            UnitBatchedAppointmentNoticeAdjuster(
                mutableMapOf<String, Double>("south" to 0.84, "west" to 0.74, "north" to 1.24),
            ),
            BatchedAppointmentNoticeQuantityOffsetAdjuster(1.67),
            FlaggedBatchedAppointmentNoticeAdjuster(flags, "batched-appointment-notice-legacy", 6.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 65058.6,
    )
}
