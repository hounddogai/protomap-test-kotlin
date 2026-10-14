package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped qualification reservation calculation. */
interface CappedQualificationReservationModifier {
    fun describe(): String
    fun adjust(item: CappedQualificationReservation, value: Double): Double
}

data class RoundingCappedQualificationReservationAdjuster(
    var step: Double,
) : CappedQualificationReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedQualificationReservation, value: Double): Double = roundTo(value, step)
}

data class CappedQualificationReservationRateOffsetAdjuster(
    var weight: Double,
) : CappedQualificationReservationModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: CappedQualificationReservation, value: Double): Double = value + item.rate * weight
}

data class CapCappedQualificationReservationAdjuster(var cap: Double) : CappedQualificationReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedQualificationReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the capped qualification reservation steps in order and keeps a trace of each. */
data class CappedQualificationReservationEngine(
    var steps: MutableList<CappedQualificationReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedQualificationReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedQualificationReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedQualificationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedQualificationReservationEngine(): CappedQualificationReservationEngine {
    return CappedQualificationReservationEngine(
        steps = mutableListOf<CappedQualificationReservationModifier>(
            RoundingCappedQualificationReservationAdjuster(0.5),
            CappedQualificationReservationRateOffsetAdjuster(1.21),
            CapCappedQualificationReservationAdjuster(42562.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 66447.9,
    )
}
