package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy trailer yard reservation calculation. */
interface LegacyTrailerYardReservationAdjuster {
    fun describe(): String
    fun adjust(item: LegacyTrailerYardReservation, value: Double): Double
}

data class LegacyTrailerYardReservationRateOffsetAdjuster(
    var weight: Double,
) : LegacyTrailerYardReservationAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: LegacyTrailerYardReservation, value: Double): Double = value + item.rate * weight
}

data class CapLegacyTrailerYardReservationAdjuster(var cap: Double) : LegacyTrailerYardReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTrailerYardReservation, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyTrailerYardReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTrailerYardReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTrailerYardReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLegacyTrailerYardReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTrailerYardReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTrailerYardReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameLegacyTrailerYardReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTrailerYardReservationAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyTrailerYardReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy trailer yard reservation steps in order and keeps a trace of each. */
data class LegacyTrailerYardReservationEngine(
    var steps: MutableList<LegacyTrailerYardReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyTrailerYardReservation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyTrailerYardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyTrailerYardReservationEngine(flags: Registry): LegacyTrailerYardReservationEngine {
    return LegacyTrailerYardReservationEngine(
        steps = mutableListOf<LegacyTrailerYardReservationAdjuster>(
            LegacyTrailerYardReservationRateOffsetAdjuster(1.08),
            CapLegacyTrailerYardReservationAdjuster(37017.0),
            TieredLegacyTrailerYardReservationAdjuster(
                mutableListOf<Double>(6492.0, 8047.0),
                mutableListOf<Double>(1.0, 1.18),
            ),
            FlaggedLegacyTrailerYardReservationAdjuster(flags, "legacy-trailer-yard-reservation-lenient", -1.8),
            NameLegacyTrailerYardReservationAdjuster(
                mutableMapOf<String, Double>("central" to 0.74, "south" to 1.08, "online" to 1.29),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 73471.8,
    )
}
