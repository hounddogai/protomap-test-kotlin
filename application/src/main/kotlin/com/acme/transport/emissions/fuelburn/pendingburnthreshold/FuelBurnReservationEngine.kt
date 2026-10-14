package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn reservation calculation. */
interface FuelBurnReservationModifier {
    fun describe(): String
    fun adjust(item: FuelBurnReservation, value: Double): Double
}

data class RoundingFuelBurnReservationAdjuster(var step: Double) : FuelBurnReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnReservation, value: Double): Double = roundTo(value, step)
}

data class PercentFuelBurnReservationAdjuster(var percent: Double) : FuelBurnReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFuelBurnReservationAdjuster(var minimum: Double) : FuelBurnReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFuelBurnReservationAdjuster(var cap: Double) : FuelBurnReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnReservation, value: Double): Double = minOf(value, cap)
}

data class FuelBurnReservationPriorityOffsetAdjuster(var weight: Double) : FuelBurnReservationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: FuelBurnReservation, value: Double): Double = value + item.priority.toDouble() * weight
}

data class ChannelFuelBurnReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelBurnReservationModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: FuelBurnReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel burn reservation steps in order and keeps a trace of each. */
data class FuelBurnReservationEngine(
    var steps: MutableList<FuelBurnReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelBurnReservation): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelBurnReservationEngine(): FuelBurnReservationEngine {
    return FuelBurnReservationEngine(
        steps = mutableListOf<FuelBurnReservationModifier>(
            RoundingFuelBurnReservationAdjuster(1.0),
            PercentFuelBurnReservationAdjuster(-10.6),
            MinimumFuelBurnReservationAdjuster(18.3),
            CapFuelBurnReservationAdjuster(12976.0),
            FuelBurnReservationPriorityOffsetAdjuster(0.23),
            ChannelFuelBurnReservationAdjuster(mutableMapOf<String, Double>("east" to 1.0, "retail" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 81661.8,
    )
}
