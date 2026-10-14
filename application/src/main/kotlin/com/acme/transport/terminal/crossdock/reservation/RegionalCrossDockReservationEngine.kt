package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional cross dock reservation calculation. */
interface RegionalCrossDockReservationAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCrossDockReservation, value: Double): Double
}

data class CapRegionalCrossDockReservationAdjuster(var cap: Double) : RegionalCrossDockReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCrossDockReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingRegionalCrossDockReservationAdjuster(
    var step: Double,
) : RegionalCrossDockReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCrossDockReservation, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalCrossDockReservationAdjuster(
    var percent: Double,
) : RegionalCrossDockReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCrossDockReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalCrossDockReservationAdjuster(
    var minimum: Double,
) : RegionalCrossDockReservationAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCrossDockReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional cross dock reservation steps in order and keeps a trace of each. */
data class RegionalCrossDockReservationEngine(
    var steps: MutableList<RegionalCrossDockReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalCrossDockReservation): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCrossDockReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCrossDockReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalCrossDockReservationEngine(): RegionalCrossDockReservationEngine {
    return RegionalCrossDockReservationEngine(
        steps = mutableListOf<RegionalCrossDockReservationAdjuster>(
            CapRegionalCrossDockReservationAdjuster(31105.0),
            RoundingRegionalCrossDockReservationAdjuster(1.0),
            PercentRegionalCrossDockReservationAdjuster(-9.7),
            MinimumRegionalCrossDockReservationAdjuster(48.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 68929.2,
    )
}
