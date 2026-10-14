package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict capacity reservation calculation. */
interface StrictCapacityReservationStep {
    fun describe(): String
    fun adjust(item: StrictCapacityReservation, value: Double): Double
}

data class NameStrictCapacityReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictCapacityReservationStep {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictCapacityReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictCapacityReservationAdjuster(var percent: Double) : StrictCapacityReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictCapacityReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredStrictCapacityReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCapacityReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCapacityReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict capacity reservation steps in order and keeps a trace of each. */
data class StrictCapacityReservationEngine(
    var steps: MutableList<StrictCapacityReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictCapacityReservation): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCapacityReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictCapacityReservationEngine(): StrictCapacityReservationEngine {
    return StrictCapacityReservationEngine(
        steps = mutableListOf<StrictCapacityReservationStep>(
            NameStrictCapacityReservationAdjuster(
                mutableMapOf<String, Double>("central" to 1.08, "north" to 0.88),
            ),
            PercentStrictCapacityReservationAdjuster(8.0),
            TieredStrictCapacityReservationAdjuster(
                mutableListOf<Double>(424.0, 3815.0, 6195.0, 9031.0),
                mutableListOf<Double>(0.86, 1.2, 1.11, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 64667.2,
    )
}
