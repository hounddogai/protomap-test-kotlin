package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict appointment summary calculation. */
interface StrictAppointmentSummaryAdjuster {
    fun describe(): String
    fun adjust(item: StrictAppointmentSummary, value: Double): Double
}

data class TitleStrictAppointmentSummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictAppointmentSummaryAdjuster(var minimum: Double) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentStrictAppointmentSummaryAdjuster(var percent: Double) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictAppointmentSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictAppointmentSummaryCeilingOffsetAdjuster(
    var weight: Double,
) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double = value + item.ceiling * weight
}

data class RoundingStrictAppointmentSummaryAdjuster(var step: Double) : StrictAppointmentSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAppointmentSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the strict appointment summary steps in order and keeps a trace of each. */
data class StrictAppointmentSummaryEngine(
    var steps: MutableList<StrictAppointmentSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictAppointmentSummary): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictAppointmentSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictAppointmentSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictAppointmentSummaryEngine(flags: Registry): StrictAppointmentSummaryEngine {
    return StrictAppointmentSummaryEngine(
        steps = mutableListOf<StrictAppointmentSummaryAdjuster>(
            TitleStrictAppointmentSummaryAdjuster(
                mutableMapOf<String, Double>("online" to 1.24, "west" to 0.88, "north" to 1.03),
            ),
            MinimumStrictAppointmentSummaryAdjuster(36.1),
            PercentStrictAppointmentSummaryAdjuster(17.7),
            FlaggedStrictAppointmentSummaryAdjuster(flags, "strict-appointment-summary-adaptive", -3.9),
            StrictAppointmentSummaryCeilingOffsetAdjuster(0.06),
            RoundingStrictAppointmentSummaryAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 41316.9,
    )
}
