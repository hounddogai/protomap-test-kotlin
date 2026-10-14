package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient drop reservation calculation. */
interface LenientDropReservationStep {
    fun describe(): String
    fun adjust(item: LenientDropReservation, value: Double): Double
}

data class LabelLenientDropReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientDropReservationStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientDropReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientDropReservationAdjuster(var minimum: Double) : LenientDropReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientDropReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientDropReservationBatchSizeOffsetAdjuster(var weight: Double) : LenientDropReservationStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientDropReservation, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapLenientDropReservationAdjuster(var cap: Double) : LenientDropReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientDropReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientDropReservationAdjuster(var step: Double) : LenientDropReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientDropReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient drop reservation steps in order and keeps a trace of each. */
data class LenientDropReservationEngine(
    var steps: MutableList<LenientDropReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientDropReservation): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientDropReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientDropReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientDropReservationEngine(): LenientDropReservationEngine {
    return LenientDropReservationEngine(
        steps = mutableListOf<LenientDropReservationStep>(
            LabelLenientDropReservationAdjuster(
                mutableMapOf<String, Double>("online" to 1.27, "central" to 0.98),
            ),
            MinimumLenientDropReservationAdjuster(41.5),
            LenientDropReservationBatchSizeOffsetAdjuster(1.82),
            CapLenientDropReservationAdjuster(40037.0),
            RoundingLenientDropReservationAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 49966.5,
    )
}
