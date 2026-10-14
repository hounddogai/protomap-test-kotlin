package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the near miss reservation calculation. */
interface NearMissReservationStep {
    fun describe(): String
    fun adjust(item: NearMissReservation, value: Double): Double
}

data class PercentNearMissReservationAdjuster(var percent: Double) : NearMissReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NearMissReservationCapacityOffsetAdjuster(var weight: Double) : NearMissReservationStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: NearMissReservation, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredNearMissReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingNearMissReservationAdjuster(var step: Double) : NearMissReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the near miss reservation steps in order and keeps a trace of each. */
data class NearMissReservationEngine(
    var steps: MutableList<NearMissReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissReservationEngine(): NearMissReservationEngine {
    return NearMissReservationEngine(
        steps = mutableListOf<NearMissReservationStep>(
            PercentNearMissReservationAdjuster(14.8),
            NearMissReservationCapacityOffsetAdjuster(0.68),
            TieredNearMissReservationAdjuster(
                mutableListOf<Double>(2195.0, 7771.0, 8519.0),
                mutableListOf<Double>(0.91, 0.98, 0.82),
            ),
            RoundingNearMissReservationAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 50099.5,
    )
}
