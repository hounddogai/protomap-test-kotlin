package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy appointment digest calculation. */
interface LegacyAppointmentDigestStep {
    fun describe(): String
    fun adjust(item: LegacyAppointmentDigest, value: Double): Double
}

data class SourceLegacyAppointmentDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAppointmentDigestStep {
    override fun describe(): String = "by source"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyAppointmentDigestAdjuster(var percent: Double) : LegacyAppointmentDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyAppointmentDigestAdjuster(var cap: Double) : LegacyAppointmentDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyAppointmentDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyAppointmentDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyAppointmentDigestAdjuster(var step: Double) : LegacyAppointmentDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyAppointmentDigestAdjuster(var minimum: Double) : LegacyAppointmentDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAppointmentDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy appointment digest steps in order and keeps a trace of each. */
data class LegacyAppointmentDigestEngine(
    var steps: MutableList<LegacyAppointmentDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyAppointmentDigest): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyAppointmentDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyAppointmentDigestEngine(flags: Registry): LegacyAppointmentDigestEngine {
    return LegacyAppointmentDigestEngine(
        steps = mutableListOf<LegacyAppointmentDigestStep>(
            SourceLegacyAppointmentDigestAdjuster(mutableMapOf<String, Double>("online" to 0.8)),
            PercentLegacyAppointmentDigestAdjuster(-3.7),
            CapLegacyAppointmentDigestAdjuster(2574.0),
            FlaggedLegacyAppointmentDigestAdjuster(flags, "legacy-appointment-digest-fallback", -6.6),
            RoundingLegacyAppointmentDigestAdjuster(0.1),
            MinimumLegacyAppointmentDigestAdjuster(30.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 55098.8,
    )
}
