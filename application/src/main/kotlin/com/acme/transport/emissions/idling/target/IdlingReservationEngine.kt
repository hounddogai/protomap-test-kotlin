package com.acme.transport.emissions.idling.target

/** One step of the idling reservation calculation. */
interface IdlingReservationStep {
    fun describe(): String
    fun adjust(item: IdlingReservation, value: Double): Double
}

data class MinimumIdlingReservationAdjuster(var minimum: Double) : IdlingReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class IdlingReservationCreatedAtOffsetAdjuster(var weight: Double) : IdlingReservationStep {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: IdlingReservation, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class CapIdlingReservationAdjuster(var cap: Double) : IdlingReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the idling reservation steps in order and keeps a trace of each. */
data class IdlingReservationEngine(
    var steps: MutableList<IdlingReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingReservation): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingReservationEngine(): IdlingReservationEngine {
    return IdlingReservationEngine(
        steps = mutableListOf<IdlingReservationStep>(
            MinimumIdlingReservationAdjuster(13.8),
            IdlingReservationCreatedAtOffsetAdjuster(1.49),
            CapIdlingReservationAdjuster(15006.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 22564.5,
    )
}
