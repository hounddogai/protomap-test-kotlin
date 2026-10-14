package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.flags.Registry

/** One step of the legacy tire reservation calculation. */
interface LegacyTireReservationModifier {
    fun describe(): String
    fun adjust(item: LegacyTireReservation, value: Double): Double
}

data class NameLegacyTireReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyTireReservationModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyTireReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyTireReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTireReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTireReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyTireReservationAdjuster(var cap: Double) : LegacyTireReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyTireReservation, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyTireReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTireReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTireReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy tire reservation steps in order and keeps a trace of each. */
data class LegacyTireReservationEngine(
    var steps: MutableList<LegacyTireReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyTireReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTireReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTireReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyTireReservationEngine(flags: Registry): LegacyTireReservationEngine {
    return LegacyTireReservationEngine(
        steps = mutableListOf<LegacyTireReservationModifier>(
            NameLegacyTireReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.05, "east" to 1.11, "north" to 1.2),
            ),
            FlaggedLegacyTireReservationAdjuster(flags, "legacy-tire-reservation-pending", -9.4),
            CapLegacyTireReservationAdjuster(46451.0),
            TieredLegacyTireReservationAdjuster(
                mutableListOf<Double>(3211.0, 5331.0, 8196.0),
                mutableListOf<Double>(0.93, 0.92, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 63407.0,
    )
}
