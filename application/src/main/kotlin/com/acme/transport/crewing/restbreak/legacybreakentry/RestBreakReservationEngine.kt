package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break reservation calculation. */
interface RestBreakReservationModifier {
    fun describe(): String
    fun adjust(item: RestBreakReservation, value: Double): Double
}

data class TieredRestBreakReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakReservationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RestBreakReservationSequenceOffsetAdjuster(var weight: Double) : RestBreakReservationModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: RestBreakReservation, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class FlaggedRestBreakReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakReservationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break reservation steps in order and keeps a trace of each. */
data class RestBreakReservationEngine(
    var steps: MutableList<RestBreakReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakReservation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakReservationEngine(flags: Registry): RestBreakReservationEngine {
    return RestBreakReservationEngine(
        steps = mutableListOf<RestBreakReservationModifier>(
            TieredRestBreakReservationAdjuster(
                mutableListOf<Double>(687.0, 3484.0, 6785.0, 9729.0),
                mutableListOf<Double>(1.09, 0.95, 1.1, 1.12),
            ),
            RestBreakReservationSequenceOffsetAdjuster(1.28),
            FlaggedRestBreakReservationAdjuster(flags, "rest-break-reservation-manual", 2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 15001.4,
    )
}
