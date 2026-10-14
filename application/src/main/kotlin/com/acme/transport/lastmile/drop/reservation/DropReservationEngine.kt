package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.numbers.roundTo

/** One step of the drop reservation calculation. */
interface DropReservationStep {
    fun describe(): String
    fun adjust(item: DropReservation, value: Double): Double
}

data class LabelDropReservationAdjuster(var factors: MutableMap<String, Double>) : DropReservationStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DropReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDropReservationAdjuster(var minimum: Double) : DropReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DropReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DropReservationPriorityOffsetAdjuster(var weight: Double) : DropReservationStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: DropReservation, value: Double): Double = value + item.priority.toDouble() * weight
}

data class RoundingDropReservationAdjuster(var step: Double) : DropReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DropReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the drop reservation steps in order and keeps a trace of each. */
data class DropReservationEngine(
    var steps: MutableList<DropReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DropReservation): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DropReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<DropReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDropReservationEngine(): DropReservationEngine {
    return DropReservationEngine(
        steps = mutableListOf<DropReservationStep>(
            LabelDropReservationAdjuster(mutableMapOf<String, Double>("west" to 1.08, "east" to 1.21)),
            MinimumDropReservationAdjuster(46.4),
            DropReservationPriorityOffsetAdjuster(1.99),
            RoundingDropReservationAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 58967.3,
    )
}
