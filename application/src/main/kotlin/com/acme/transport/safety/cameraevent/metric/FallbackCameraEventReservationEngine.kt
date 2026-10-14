package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback camera event reservation calculation. */
interface FallbackCameraEventReservationAdjuster {
    fun describe(): String
    fun adjust(item: FallbackCameraEventReservation, value: Double): Double
}

data class SourceFallbackCameraEventReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackCameraEventReservationAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: FallbackCameraEventReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFallbackCameraEventReservationAdjuster(var cap: Double) : FallbackCameraEventReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackCameraEventReservation, value: Double): Double = minOf(value, cap)
}

data class FallbackCameraEventReservationFuelLitersOffsetAdjuster(
    var weight: Double,
) : FallbackCameraEventReservationAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: FallbackCameraEventReservation, value: Double): Double {
        return value + item.fuelLiters * weight
    }
}

data class RoundingFallbackCameraEventReservationAdjuster(
    var step: Double,
) : FallbackCameraEventReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackCameraEventReservation, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackCameraEventReservationAdjuster(
    var percent: Double,
) : FallbackCameraEventReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackCameraEventReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback camera event reservation steps in order and keeps a trace of each. */
data class FallbackCameraEventReservationEngine(
    var steps: MutableList<FallbackCameraEventReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackCameraEventReservation): Double {
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

    fun evaluateAll(items: MutableList<FallbackCameraEventReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackCameraEventReservationEngine(): FallbackCameraEventReservationEngine {
    return FallbackCameraEventReservationEngine(
        steps = mutableListOf<FallbackCameraEventReservationAdjuster>(
            SourceFallbackCameraEventReservationAdjuster(
                mutableMapOf<String, Double>("west" to 0.96, "south" to 0.98),
            ),
            CapFallbackCameraEventReservationAdjuster(20363.0),
            FallbackCameraEventReservationFuelLitersOffsetAdjuster(1.3),
            RoundingFallbackCameraEventReservationAdjuster(0.05),
            PercentFallbackCameraEventReservationAdjuster(-6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 48167.0,
    )
}
