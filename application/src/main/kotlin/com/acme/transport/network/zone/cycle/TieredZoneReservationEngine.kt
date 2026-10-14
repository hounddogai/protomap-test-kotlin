package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the tiered zone reservation calculation. */
interface TieredZoneReservationAdjuster {
    fun describe(): String
    fun adjust(item: TieredZoneReservation, value: Double): Double
}

data class PercentTieredZoneReservationAdjuster(var percent: Double) : TieredZoneReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredZoneReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredZoneReservationFuelLitersOffsetAdjuster(var weight: Double) : TieredZoneReservationAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TieredZoneReservation, value: Double): Double = value + item.fuelLiters * weight
}

data class FlaggedTieredZoneReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredZoneReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredZoneReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredZoneReservationAdjuster(var cap: Double) : TieredZoneReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredZoneReservation, value: Double): Double = minOf(value, cap)
}

data class TitleTieredZoneReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredZoneReservationAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredZoneReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered zone reservation steps in order and keeps a trace of each. */
data class TieredZoneReservationEngine(
    var steps: MutableList<TieredZoneReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredZoneReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredZoneReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredZoneReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredZoneReservationEngine(flags: Registry): TieredZoneReservationEngine {
    return TieredZoneReservationEngine(
        steps = mutableListOf<TieredZoneReservationAdjuster>(
            PercentTieredZoneReservationAdjuster(-4.0),
            TieredZoneReservationFuelLitersOffsetAdjuster(1.88),
            FlaggedTieredZoneReservationAdjuster(flags, "tiered-zone-reservation-weighted", 9.0),
            CapTieredZoneReservationAdjuster(9914.0),
            TitleTieredZoneReservationAdjuster(
                mutableMapOf<String, Double>("north" to 1.34, "online" to 1.33, "west" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19593.8,
    )
}
