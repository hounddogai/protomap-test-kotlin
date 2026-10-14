package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback backhaul reservation calculation. */
interface FallbackBackhaulReservationAdjuster {
    fun describe(): String
    fun adjust(item: FallbackBackhaulReservation, value: Double): Double
}

data class MinimumFallbackBackhaulReservationAdjuster(
    var minimum: Double,
) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionFallbackBackhaulReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackBackhaulReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackBackhaulReservationAdjuster(
    var percent: Double,
) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackBackhaulReservationAdjuster(var cap: Double) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackBackhaulReservationAdjuster(var step: Double) : FallbackBackhaulReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackBackhaulReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the fallback backhaul reservation steps in order and keeps a trace of each. */
data class FallbackBackhaulReservationEngine(
    var steps: MutableList<FallbackBackhaulReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackBackhaulReservation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackBackhaulReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackBackhaulReservationEngine(flags: Registry): FallbackBackhaulReservationEngine {
    return FallbackBackhaulReservationEngine(
        steps = mutableListOf<FallbackBackhaulReservationAdjuster>(
            MinimumFallbackBackhaulReservationAdjuster(48.9),
            RegionFallbackBackhaulReservationAdjuster(
                mutableMapOf<String, Double>("south" to 1.2, "east" to 0.74),
            ),
            FlaggedFallbackBackhaulReservationAdjuster(flags, "fallback-backhaul-reservation-fallback", 1.9),
            PercentFallbackBackhaulReservationAdjuster(19.7),
            CapFallbackBackhaulReservationAdjuster(16333.0),
            RoundingFallbackBackhaulReservationAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 46886.6,
    )
}
