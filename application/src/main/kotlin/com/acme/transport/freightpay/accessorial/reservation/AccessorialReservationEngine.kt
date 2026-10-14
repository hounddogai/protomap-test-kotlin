package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial reservation calculation. */
interface AccessorialReservationFactor {
    fun describe(): String
    fun adjust(item: AccessorialReservation, value: Double): Double
}

data class FlaggedAccessorialReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAccessorialReservationAdjuster(var step: Double) : AccessorialReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialReservation, value: Double): Double = roundTo(value, step)
}

data class CapAccessorialReservationAdjuster(var cap: Double) : AccessorialReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialReservation, value: Double): Double = minOf(value, cap)
}

data class AccessorialReservationRateOffsetAdjuster(var weight: Double) : AccessorialReservationFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: AccessorialReservation, value: Double): Double = value + item.rate * weight
}

data class MinimumAccessorialReservationAdjuster(var minimum: Double) : AccessorialReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the accessorial reservation steps in order and keeps a trace of each. */
data class AccessorialReservationEngine(
    var steps: MutableList<AccessorialReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialReservation): Double {
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

    fun evaluateAll(items: MutableList<AccessorialReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialReservationEngine(flags: Registry): AccessorialReservationEngine {
    return AccessorialReservationEngine(
        steps = mutableListOf<AccessorialReservationFactor>(
            FlaggedAccessorialReservationAdjuster(flags, "accessorial-reservation-regional", 5.5),
            RoundingAccessorialReservationAdjuster(0.5),
            CapAccessorialReservationAdjuster(39638.0),
            AccessorialReservationRateOffsetAdjuster(1.32),
            MinimumAccessorialReservationAdjuster(45.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 42638.3,
    )
}
