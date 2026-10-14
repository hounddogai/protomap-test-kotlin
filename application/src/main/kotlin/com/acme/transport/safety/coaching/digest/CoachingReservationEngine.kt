package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the coaching reservation calculation. */
interface CoachingReservationFactor {
    fun describe(): String
    fun adjust(item: CoachingReservation, value: Double): Double
}

data class PercentCoachingReservationAdjuster(var percent: Double) : CoachingReservationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCoachingReservationAdjuster(var minimum: Double) : CoachingReservationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCoachingReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCoachingReservationAdjuster(var step: Double) : CoachingReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingReservation, value: Double): Double = roundTo(value, step)
}

data class TieredCoachingReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCoachingReservationAdjuster(var cap: Double) : CoachingReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CoachingReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the coaching reservation steps in order and keeps a trace of each. */
data class CoachingReservationEngine(
    var steps: MutableList<CoachingReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CoachingReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCoachingReservationEngine(flags: Registry): CoachingReservationEngine {
    return CoachingReservationEngine(
        steps = mutableListOf<CoachingReservationFactor>(
            PercentCoachingReservationAdjuster(23.8),
            MinimumCoachingReservationAdjuster(36.5),
            FlaggedCoachingReservationAdjuster(flags, "coaching-reservation-regional", 6.5),
            RoundingCoachingReservationAdjuster(0.25),
            TieredCoachingReservationAdjuster(
                mutableListOf<Double>(4568.0, 7291.0, 8394.0),
                mutableListOf<Double>(1.13, 0.98, 1.01),
            ),
            CapCoachingReservationAdjuster(45217.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 84307.0,
    )
}
