package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

/** One step of the strict rate card reservation calculation. */
interface StrictRateCardReservationModifier {
    fun describe(): String
    fun adjust(item: StrictRateCardReservation, value: Double): Double
}

data class MinimumStrictRateCardReservationAdjuster(var minimum: Double) : StrictRateCardReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRateCardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictRateCardReservationPriorityOffsetAdjuster(
    var weight: Double,
) : StrictRateCardReservationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: StrictRateCardReservation, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class CapStrictRateCardReservationAdjuster(var cap: Double) : StrictRateCardReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRateCardReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the strict rate card reservation steps in order and keeps a trace of each. */
data class StrictRateCardReservationEngine(
    var steps: MutableList<StrictRateCardReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRateCardReservation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRateCardReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRateCardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRateCardReservationEngine(): StrictRateCardReservationEngine {
    return StrictRateCardReservationEngine(
        steps = mutableListOf<StrictRateCardReservationModifier>(
            MinimumStrictRateCardReservationAdjuster(39.3),
            StrictRateCardReservationPriorityOffsetAdjuster(0.43),
            CapStrictRateCardReservationAdjuster(21276.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 93589.9,
    )
}
