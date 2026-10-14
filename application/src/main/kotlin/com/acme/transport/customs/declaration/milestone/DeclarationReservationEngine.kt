package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration reservation calculation. */
interface DeclarationReservationModifier {
    fun describe(): String
    fun adjust(item: DeclarationReservation, value: Double): Double
}

data class RoundingDeclarationReservationAdjuster(var step: Double) : DeclarationReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationReservation, value: Double): Double = roundTo(value, step)
}

data class DeclarationReservationRateOffsetAdjuster(var weight: Double) : DeclarationReservationModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: DeclarationReservation, value: Double): Double = value + item.rate * weight
}

data class TieredDeclarationReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDeclarationReservationAdjuster(var cap: Double) : DeclarationReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the declaration reservation steps in order and keeps a trace of each. */
data class DeclarationReservationEngine(
    var steps: MutableList<DeclarationReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DeclarationReservation): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDeclarationReservationEngine(): DeclarationReservationEngine {
    return DeclarationReservationEngine(
        steps = mutableListOf<DeclarationReservationModifier>(
            RoundingDeclarationReservationAdjuster(1.0),
            DeclarationReservationRateOffsetAdjuster(1.39),
            TieredDeclarationReservationAdjuster(
                mutableListOf<Double>(3717.0, 5971.0, 6442.0, 8909.0),
                mutableListOf<Double>(1.08, 1.15, 1.06, 0.86),
            ),
            CapDeclarationReservationAdjuster(43319.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 81374.4,
    )
}
