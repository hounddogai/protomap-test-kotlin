package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the appointment draft calculation. */
interface AppointmentDraftFactor {
    fun describe(): String
    fun adjust(item: AppointmentDraft, value: Double): Double
}

data class AppointmentDraftThresholdOffsetAdjuster(var weight: Double) : AppointmentDraftFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: AppointmentDraft, value: Double): Double = value + item.threshold * weight
}

data class NameAppointmentDraftAdjuster(var factors: MutableMap<String, Double>) : AppointmentDraftFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: AppointmentDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAppointmentDraftAdjuster(var percent: Double) : AppointmentDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AppointmentDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAppointmentDraftAdjuster(var minimum: Double) : AppointmentDraftFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AppointmentDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAppointmentDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AppointmentDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AppointmentDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the appointment draft steps in order and keeps a trace of each. */
data class AppointmentDraftEngine(
    var steps: MutableList<AppointmentDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AppointmentDraft): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AppointmentDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAppointmentDraftEngine(flags: Registry): AppointmentDraftEngine {
    return AppointmentDraftEngine(
        steps = mutableListOf<AppointmentDraftFactor>(
            AppointmentDraftThresholdOffsetAdjuster(0.8),
            NameAppointmentDraftAdjuster(mutableMapOf<String, Double>("central" to 0.93, "retail" to 0.8)),
            PercentAppointmentDraftAdjuster(15.9),
            MinimumAppointmentDraftAdjuster(45.1),
            FlaggedAppointmentDraftAdjuster(flags, "appointment-draft-primary", 5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 12068.2,
    )
}
