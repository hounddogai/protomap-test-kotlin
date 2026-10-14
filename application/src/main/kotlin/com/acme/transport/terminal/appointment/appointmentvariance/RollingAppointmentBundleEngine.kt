package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling appointment bundle calculation. */
interface RollingAppointmentBundleFactor {
    fun describe(): String
    fun adjust(item: RollingAppointmentBundle, value: Double): Double
}

data class MinimumRollingAppointmentBundleAdjuster(var minimum: Double) : RollingAppointmentBundleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingAppointmentBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRollingAppointmentBundleAdjuster(var cap: Double) : RollingAppointmentBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingAppointmentBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedRollingAppointmentBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAppointmentBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAppointmentBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingAppointmentBundleAdjuster(var percent: Double) : RollingAppointmentBundleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAppointmentBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling appointment bundle steps in order and keeps a trace of each. */
data class RollingAppointmentBundleEngine(
    var steps: MutableList<RollingAppointmentBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingAppointmentBundle): Double {
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

    fun evaluateAll(items: MutableList<RollingAppointmentBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingAppointmentBundleEngine(flags: Registry): RollingAppointmentBundleEngine {
    return RollingAppointmentBundleEngine(
        steps = mutableListOf<RollingAppointmentBundleFactor>(
            MinimumRollingAppointmentBundleAdjuster(17.4),
            CapRollingAppointmentBundleAdjuster(12930.0),
            FlaggedRollingAppointmentBundleAdjuster(flags, "rolling-appointment-bundle-regional", -6.1),
            PercentRollingAppointmentBundleAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 36967.1,
    )
}
