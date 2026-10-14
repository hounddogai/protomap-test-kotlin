package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization reservation calculation. */
interface UtilizationReservationStep {
    fun describe(): String
    fun adjust(item: UtilizationReservation, value: Double): Double
}

data class PercentUtilizationReservationAdjuster(var percent: Double) : UtilizationReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingUtilizationReservationAdjuster(var step: Double) : UtilizationReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationReservation, value: Double): Double = roundTo(value, step)
}

data class UtilizationReservationExpiresAtOffsetAdjuster(var weight: Double) : UtilizationReservationStep {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: UtilizationReservation, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class MinimumUtilizationReservationAdjuster(var minimum: Double) : UtilizationReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the utilization reservation steps in order and keeps a trace of each. */
data class UtilizationReservationEngine(
    var steps: MutableList<UtilizationReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationReservation): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationReservationEngine(): UtilizationReservationEngine {
    return UtilizationReservationEngine(
        steps = mutableListOf<UtilizationReservationStep>(
            PercentUtilizationReservationAdjuster(10.4),
            RoundingUtilizationReservationAdjuster(1.0),
            UtilizationReservationExpiresAtOffsetAdjuster(0.55),
            MinimumUtilizationReservationAdjuster(37.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 54588.1,
    )
}
