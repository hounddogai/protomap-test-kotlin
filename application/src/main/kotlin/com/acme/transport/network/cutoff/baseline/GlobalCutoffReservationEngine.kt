package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.numbers.roundTo

/** One step of the global cutoff reservation calculation. */
interface GlobalCutoffReservationModifier {
    fun describe(): String
    fun adjust(item: GlobalCutoffReservation, value: Double): Double
}

data class CapGlobalCutoffReservationAdjuster(var cap: Double) : GlobalCutoffReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCutoffReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingGlobalCutoffReservationAdjuster(var step: Double) : GlobalCutoffReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCutoffReservation, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalCutoffReservationAdjuster(var minimum: Double) : GlobalCutoffReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCutoffReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalCutoffReservationAdjuster(var percent: Double) : GlobalCutoffReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCutoffReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global cutoff reservation steps in order and keeps a trace of each. */
data class GlobalCutoffReservationEngine(
    var steps: MutableList<GlobalCutoffReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalCutoffReservation): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCutoffReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCutoffReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalCutoffReservationEngine(): GlobalCutoffReservationEngine {
    return GlobalCutoffReservationEngine(
        steps = mutableListOf<GlobalCutoffReservationModifier>(
            CapGlobalCutoffReservationAdjuster(11388.0),
            RoundingGlobalCutoffReservationAdjuster(0.05),
            MinimumGlobalCutoffReservationAdjuster(24.4),
            PercentGlobalCutoffReservationAdjuster(5.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 23725.6,
    )
}
