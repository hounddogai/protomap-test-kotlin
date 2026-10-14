package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff reservation calculation. */
interface TariffReservationFactor {
    fun describe(): String
    fun adjust(item: TariffReservation, value: Double): Double
}

data class RoundingTariffReservationAdjuster(var step: Double) : TariffReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffReservation, value: Double): Double = roundTo(value, step)
}

data class TariffReservationExpiresAtOffsetAdjuster(var weight: Double) : TariffReservationFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: TariffReservation, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class NameTariffReservationAdjuster(var factors: MutableMap<String, Double>) : TariffReservationFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TariffReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTariffReservationAdjuster(var percent: Double) : TariffReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTariffReservationAdjuster(var minimum: Double) : TariffReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTariffReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tariff reservation steps in order and keeps a trace of each. */
data class TariffReservationEngine(
    var steps: MutableList<TariffReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffReservationEngine(flags: Registry): TariffReservationEngine {
    return TariffReservationEngine(
        steps = mutableListOf<TariffReservationFactor>(
            RoundingTariffReservationAdjuster(0.5),
            TariffReservationExpiresAtOffsetAdjuster(0.81),
            NameTariffReservationAdjuster(mutableMapOf<String, Double>("retail" to 0.81, "west" to 1.32)),
            PercentTariffReservationAdjuster(-15.9),
            MinimumTariffReservationAdjuster(42.7),
            FlaggedTariffReservationAdjuster(flags, "tariff-reservation-rolling", 8.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 32649.2,
    )
}
