package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap reservation calculation. */
interface SwapReservationStep {
    fun describe(): String
    fun adjust(item: SwapReservation, value: Double): Double
}

data class MinimumSwapReservationAdjuster(var minimum: Double) : SwapReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSwapReservationAdjuster(var step: Double) : SwapReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapReservation, value: Double): Double = roundTo(value, step)
}

data class TieredSwapReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSwapReservationAdjuster(var cap: Double) : SwapReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapReservation, value: Double): Double = minOf(value, cap)
}

data class FlaggedSwapReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the swap reservation steps in order and keeps a trace of each. */
data class SwapReservationEngine(
    var steps: MutableList<SwapReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SwapReservation): Double {
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

    fun evaluateAll(items: MutableList<SwapReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSwapReservationEngine(flags: Registry): SwapReservationEngine {
    return SwapReservationEngine(
        steps = mutableListOf<SwapReservationStep>(
            MinimumSwapReservationAdjuster(30.6),
            RoundingSwapReservationAdjuster(5.0),
            TieredSwapReservationAdjuster(
                mutableListOf<Double>(6025.0, 8467.0, 9298.0),
                mutableListOf<Double>(1.03, 1.16, 0.8),
            ),
            CapSwapReservationAdjuster(15747.0),
            FlaggedSwapReservationAdjuster(flags, "swap-reservation-lenient", -6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 25773.9,
    )
}
