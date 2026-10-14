package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal appointment audit calculation. */
interface SeasonalAppointmentAuditModifier {
    fun describe(): String
    fun adjust(item: SeasonalAppointmentAudit, value: Double): Double
}

data class MinimumSeasonalAppointmentAuditAdjuster(var minimum: Double) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSeasonalAppointmentAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SeasonalAppointmentAuditCapacityOffsetAdjuster(
    var weight: Double,
) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentSeasonalAppointmentAuditAdjuster(var percent: Double) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalAppointmentAuditAdjuster(var step: Double) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalAppointmentAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAppointmentAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAppointmentAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal appointment audit steps in order and keeps a trace of each. */
data class SeasonalAppointmentAuditEngine(
    var steps: MutableList<SeasonalAppointmentAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAppointmentAudit): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAppointmentAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAppointmentAuditEngine(flags: Registry): SeasonalAppointmentAuditEngine {
    return SeasonalAppointmentAuditEngine(
        steps = mutableListOf<SeasonalAppointmentAuditModifier>(
            MinimumSeasonalAppointmentAuditAdjuster(2.7),
            TieredSeasonalAppointmentAuditAdjuster(
                mutableListOf<Double>(1619.0, 9761.0),
                mutableListOf<Double>(0.89, 0.81),
            ),
            SeasonalAppointmentAuditCapacityOffsetAdjuster(1.64),
            PercentSeasonalAppointmentAuditAdjuster(6.6),
            RoundingSeasonalAppointmentAuditAdjuster(0.5),
            FlaggedSeasonalAppointmentAuditAdjuster(flags, "seasonal-appointment-audit-pending", -8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 3560.0,
    )
}
