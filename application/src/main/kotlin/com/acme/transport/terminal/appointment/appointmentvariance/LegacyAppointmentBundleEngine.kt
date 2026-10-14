package com.acme.transport.terminal.appointment.appointmentvariance

/** One step of the legacy appointment bundle calculation. */
interface LegacyAppointmentBundleModifier {
    fun describe(): String
    fun adjust(item: LegacyAppointmentBundle, value: Double): Double
}

data class RegionLegacyAppointmentBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAppointmentBundleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyAppointmentBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLegacyAppointmentBundleAdjuster(var minimum: Double) : LegacyAppointmentBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAppointmentBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyAppointmentBundleStartsAtOffsetAdjuster(var weight: Double) : LegacyAppointmentBundleModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: LegacyAppointmentBundle, value: Double): Double {
        return value + item.startsAt.toDouble() * weight
    }
}

data class CapLegacyAppointmentBundleAdjuster(var cap: Double) : LegacyAppointmentBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAppointmentBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy appointment bundle steps in order and keeps a trace of each. */
data class LegacyAppointmentBundleEngine(
    var steps: MutableList<LegacyAppointmentBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyAppointmentBundle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyAppointmentBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyAppointmentBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyAppointmentBundleEngine(): LegacyAppointmentBundleEngine {
    return LegacyAppointmentBundleEngine(
        steps = mutableListOf<LegacyAppointmentBundleModifier>(
            RegionLegacyAppointmentBundleAdjuster(
                mutableMapOf<String, Double>("north" to 1.08, "central" to 1.29),
            ),
            MinimumLegacyAppointmentBundleAdjuster(44.3),
            LegacyAppointmentBundleStartsAtOffsetAdjuster(1.37),
            CapLegacyAppointmentBundleAdjuster(36961.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 30791.5,
    )
}
