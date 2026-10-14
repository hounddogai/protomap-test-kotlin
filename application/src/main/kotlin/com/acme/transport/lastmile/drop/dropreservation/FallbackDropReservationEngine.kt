package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback drop reservation calculation. */
interface FallbackDropReservationStep {
    fun describe(): String
    fun adjust(item: FallbackDropReservation, value: Double): Double
}

data class RoundingFallbackDropReservationAdjuster(var step: Double) : FallbackDropReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDropReservation, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackDropReservationAdjuster(var percent: Double) : FallbackDropReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDropReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackDropReservationAdjuster(var cap: Double) : FallbackDropReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackDropReservation, value: Double): Double = minOf(value, cap)
}

data class FallbackDropReservationRevisionOffsetAdjuster(var weight: Double) : FallbackDropReservationStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: FallbackDropReservation, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class MinimumFallbackDropReservationAdjuster(var minimum: Double) : FallbackDropReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDropReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fallback drop reservation steps in order and keeps a trace of each. */
data class FallbackDropReservationEngine(
    var steps: MutableList<FallbackDropReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackDropReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackDropReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackDropReservationEngine(): FallbackDropReservationEngine {
    return FallbackDropReservationEngine(
        steps = mutableListOf<FallbackDropReservationStep>(
            RoundingFallbackDropReservationAdjuster(0.1),
            PercentFallbackDropReservationAdjuster(-10.1),
            CapFallbackDropReservationAdjuster(1398.0),
            FallbackDropReservationRevisionOffsetAdjuster(1.56),
            MinimumFallbackDropReservationAdjuster(43.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 82626.8,
    )
}
