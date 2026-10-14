package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the redelivery reservation calculation. */
interface RedeliveryReservationModifier {
    fun describe(): String
    fun adjust(item: RedeliveryReservation, value: Double): Double
}

data class DepotRedeliveryReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryReservationModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: RedeliveryReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRedeliveryReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RedeliveryReservationQuantityOffsetAdjuster(var weight: Double) : RedeliveryReservationModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RedeliveryReservation, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the redelivery reservation steps in order and keeps a trace of each. */
data class RedeliveryReservationEngine(
    var steps: MutableList<RedeliveryReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryReservation): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RedeliveryReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryReservationEngine(flags: Registry): RedeliveryReservationEngine {
    return RedeliveryReservationEngine(
        steps = mutableListOf<RedeliveryReservationModifier>(
            DepotRedeliveryReservationAdjuster(mutableMapOf<String, Double>("west" to 0.75, "online" to 0.7)),
            FlaggedRedeliveryReservationAdjuster(flags, "redelivery-reservation-weighted", 8.8),
            RedeliveryReservationQuantityOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 32444.0,
    )
}
