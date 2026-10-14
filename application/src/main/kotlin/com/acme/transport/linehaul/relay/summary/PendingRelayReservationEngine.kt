package com.acme.transport.linehaul.relay.summary

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending relay reservation calculation. */
interface PendingRelayReservationModifier {
    fun describe(): String
    fun adjust(item: PendingRelayReservation, value: Double): Double
}

data class MinimumPendingRelayReservationAdjuster(var minimum: Double) : PendingRelayReservationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingRelayReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingRelayReservationDurationMinutesOffsetAdjuster(
    var weight: Double,
) : PendingRelayReservationModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: PendingRelayReservation, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TitlePendingRelayReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingRelayReservationModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingRelayReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentPendingRelayReservationAdjuster(var percent: Double) : PendingRelayReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingRelayReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingRelayReservationAdjuster(var step: Double) : PendingRelayReservationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingRelayReservation, value: Double): Double = roundTo(value, step)
}

/** Runs the pending relay reservation steps in order and keeps a trace of each. */
data class PendingRelayReservationEngine(
    var steps: MutableList<PendingRelayReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingRelayReservation): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingRelayReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingRelayReservationEngine(): PendingRelayReservationEngine {
    return PendingRelayReservationEngine(
        steps = mutableListOf<PendingRelayReservationModifier>(
            MinimumPendingRelayReservationAdjuster(16.5),
            PendingRelayReservationDurationMinutesOffsetAdjuster(1.61),
            TitlePendingRelayReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.27, "south" to 0.75),
            ),
            PercentPendingRelayReservationAdjuster(-22.7),
            RoundingPendingRelayReservationAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 41654.8,
    )
}
