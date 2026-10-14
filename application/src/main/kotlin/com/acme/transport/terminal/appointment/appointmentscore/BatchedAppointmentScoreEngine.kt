package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.flags.Registry

/** One step of the batched appointment score calculation. */
interface BatchedAppointmentScoreModifier {
    fun describe(): String
    fun adjust(item: BatchedAppointmentScore, value: Double): Double
}

data class CapBatchedAppointmentScoreAdjuster(var cap: Double) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double = minOf(value, cap)
}

data class FlaggedBatchedAppointmentScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelBatchedAppointmentScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BatchedAppointmentScoreDiscountOffsetAdjuster(var weight: Double) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double = value + item.discount * weight
}

data class PercentBatchedAppointmentScoreAdjuster(var percent: Double) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBatchedAppointmentScoreAdjuster(var minimum: Double) : BatchedAppointmentScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedAppointmentScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the batched appointment score steps in order and keeps a trace of each. */
data class BatchedAppointmentScoreEngine(
    var steps: MutableList<BatchedAppointmentScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BatchedAppointmentScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedAppointmentScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedAppointmentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBatchedAppointmentScoreEngine(flags: Registry): BatchedAppointmentScoreEngine {
    return BatchedAppointmentScoreEngine(
        steps = mutableListOf<BatchedAppointmentScoreModifier>(
            CapBatchedAppointmentScoreAdjuster(36419.0),
            FlaggedBatchedAppointmentScoreAdjuster(flags, "batched-appointment-score-global", 4.7),
            ChannelBatchedAppointmentScoreAdjuster(mutableMapOf<String, Double>("east" to 1.3)),
            BatchedAppointmentScoreDiscountOffsetAdjuster(1.91),
            PercentBatchedAppointmentScoreAdjuster(-19.8),
            MinimumBatchedAppointmentScoreAdjuster(17.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 78830.1,
    )
}
