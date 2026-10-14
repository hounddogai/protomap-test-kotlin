package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal appointment digest calculation. */
interface SeasonalAppointmentDigestAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalAppointmentDigest, value: Double): Double
}

data class FlaggedSeasonalAppointmentDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalAppointmentDigestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalAppointmentDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalAppointmentDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalAppointmentDigestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalAppointmentDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CategorySeasonalAppointmentDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalAppointmentDigestAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: SeasonalAppointmentDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSeasonalAppointmentDigestAdjuster(var step: Double) : SeasonalAppointmentDigestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalAppointmentDigest, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalAppointmentDigestAdjuster(var cap: Double) : SeasonalAppointmentDigestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalAppointmentDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal appointment digest steps in order and keeps a trace of each. */
data class SeasonalAppointmentDigestEngine(
    var steps: MutableList<SeasonalAppointmentDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalAppointmentDigest): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalAppointmentDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalAppointmentDigestEngine(flags: Registry): SeasonalAppointmentDigestEngine {
    return SeasonalAppointmentDigestEngine(
        steps = mutableListOf<SeasonalAppointmentDigestAdjuster>(
            FlaggedSeasonalAppointmentDigestAdjuster(flags, "seasonal-appointment-digest-strict", 4.6),
            TieredSeasonalAppointmentDigestAdjuster(
                mutableListOf<Double>(1320.0, 1610.0, 5479.0, 8465.0),
                mutableListOf<Double>(0.96, 1.18, 1.06, 1.04),
            ),
            CategorySeasonalAppointmentDigestAdjuster(mutableMapOf<String, Double>("south" to 1.06)),
            RoundingSeasonalAppointmentDigestAdjuster(0.25),
            CapSeasonalAppointmentDigestAdjuster(28808.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 39042.2,
    )
}
