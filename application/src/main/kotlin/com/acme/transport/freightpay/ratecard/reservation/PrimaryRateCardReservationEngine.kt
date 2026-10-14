package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary rate card reservation calculation. */
interface PrimaryRateCardReservationModifier {
    fun describe(): String
    fun adjust(item: PrimaryRateCardReservation, value: Double): Double
}

data class CapPrimaryRateCardReservationAdjuster(var cap: Double) : PrimaryRateCardReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRateCardReservation, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryRateCardReservationAdjuster(var minimum: Double) : PrimaryRateCardReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRateCardReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocalePrimaryRateCardReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRateCardReservationModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryRateCardReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPrimaryRateCardReservationAdjuster(var percent: Double) : PrimaryRateCardReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryRateCardReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class PrimaryRateCardReservationPriorityOffsetAdjuster(
    var weight: Double,
) : PrimaryRateCardReservationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: PrimaryRateCardReservation, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

/** Runs the primary rate card reservation steps in order and keeps a trace of each. */
data class PrimaryRateCardReservationEngine(
    var steps: MutableList<PrimaryRateCardReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryRateCardReservation): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRateCardReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryRateCardReservationEngine(): PrimaryRateCardReservationEngine {
    return PrimaryRateCardReservationEngine(
        steps = mutableListOf<PrimaryRateCardReservationModifier>(
            CapPrimaryRateCardReservationAdjuster(34965.0),
            MinimumPrimaryRateCardReservationAdjuster(34.4),
            LocalePrimaryRateCardReservationAdjuster(mutableMapOf<String, Double>("central" to 1.33)),
            PercentPrimaryRateCardReservationAdjuster(-19.2),
            PrimaryRateCardReservationPriorityOffsetAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 12586.7,
    )
}
