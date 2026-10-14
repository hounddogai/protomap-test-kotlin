package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending declaration reservation calculation. */
interface PendingDeclarationReservationFactor {
    fun describe(): String
    fun adjust(item: PendingDeclarationReservation, value: Double): Double
}

data class PendingDeclarationReservationLimitOffsetAdjuster(
    var weight: Double,
) : PendingDeclarationReservationFactor {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class RoundingPendingDeclarationReservationAdjuster(
    var step: Double,
) : PendingDeclarationReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double = roundTo(value, step)
}

data class TieredPendingDeclarationReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDeclarationReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingDeclarationReservationAdjuster(var cap: Double) : PendingDeclarationReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingDeclarationReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDeclarationReservationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelPendingDeclarationReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDeclarationReservationFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingDeclarationReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending declaration reservation steps in order and keeps a trace of each. */
data class PendingDeclarationReservationEngine(
    var steps: MutableList<PendingDeclarationReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDeclarationReservation): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDeclarationReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDeclarationReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDeclarationReservationEngine(flags: Registry): PendingDeclarationReservationEngine {
    return PendingDeclarationReservationEngine(
        steps = mutableListOf<PendingDeclarationReservationFactor>(
            PendingDeclarationReservationLimitOffsetAdjuster(1.28),
            RoundingPendingDeclarationReservationAdjuster(0.1),
            TieredPendingDeclarationReservationAdjuster(
                mutableListOf<Double>(3844.0, 4271.0),
                mutableListOf<Double>(0.83, 0.88),
            ),
            CapPendingDeclarationReservationAdjuster(37595.0),
            FlaggedPendingDeclarationReservationAdjuster(flags, "pending-declaration-reservation-legacy", 8.3),
            LabelPendingDeclarationReservationAdjuster(
                mutableMapOf<String, Double>("retail" to 1.11, "west" to 0.87, "south" to 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 7312.4,
    )
}
