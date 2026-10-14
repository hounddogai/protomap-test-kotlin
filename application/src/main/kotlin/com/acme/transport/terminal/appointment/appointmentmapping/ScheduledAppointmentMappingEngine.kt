package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled appointment mapping calculation. */
interface ScheduledAppointmentMappingFactor {
    fun describe(): String
    fun adjust(item: ScheduledAppointmentMapping, value: Double): Double
}

data class ChannelScheduledAppointmentMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledAppointmentMappingFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: ScheduledAppointmentMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledAppointmentMappingAdjuster(var step: Double) : ScheduledAppointmentMappingFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledAppointmentMapping, value: Double): Double = roundTo(value, step)
}

data class ScheduledAppointmentMappingSequenceOffsetAdjuster(
    var weight: Double,
) : ScheduledAppointmentMappingFactor {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: ScheduledAppointmentMapping, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class PercentScheduledAppointmentMappingAdjuster(var percent: Double) : ScheduledAppointmentMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAppointmentMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled appointment mapping steps in order and keeps a trace of each. */
data class ScheduledAppointmentMappingEngine(
    var steps: MutableList<ScheduledAppointmentMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledAppointmentMapping): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledAppointmentMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledAppointmentMappingEngine(): ScheduledAppointmentMappingEngine {
    return ScheduledAppointmentMappingEngine(
        steps = mutableListOf<ScheduledAppointmentMappingFactor>(
            ChannelScheduledAppointmentMappingAdjuster(
                mutableMapOf<String, Double>("retail" to 0.94, "east" to 0.71),
            ),
            RoundingScheduledAppointmentMappingAdjuster(0.25),
            ScheduledAppointmentMappingSequenceOffsetAdjuster(0.53),
            PercentScheduledAppointmentMappingAdjuster(-1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 11224.4,
    )
}
