package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional cutoff reservation calculation. */
interface RegionalCutoffReservationAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCutoffReservation, value: Double): Double
}

data class RegionalCutoffReservationOdometerOffsetAdjuster(
    var weight: Double,
) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class CapRegionalCutoffReservationAdjuster(var cap: Double) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalCutoffReservationAdjuster(var step: Double) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalCutoffReservationAdjuster(var minimum: Double) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameRegionalCutoffReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalCutoffReservationAdjuster(var percent: Double) : RegionalCutoffReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCutoffReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional cutoff reservation steps in order and keeps a trace of each. */
data class RegionalCutoffReservationEngine(
    var steps: MutableList<RegionalCutoffReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCutoffReservation): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCutoffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCutoffReservationEngine(): RegionalCutoffReservationEngine {
    return RegionalCutoffReservationEngine(
        steps = mutableListOf<RegionalCutoffReservationAdjuster>(
            RegionalCutoffReservationOdometerOffsetAdjuster(0.02),
            CapRegionalCutoffReservationAdjuster(5187.0),
            RoundingRegionalCutoffReservationAdjuster(0.25),
            MinimumRegionalCutoffReservationAdjuster(15.4),
            NameRegionalCutoffReservationAdjuster(mutableMapOf<String, Double>("north" to 1.35)),
            PercentRegionalCutoffReservationAdjuster(1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 27818.2,
    )
}
