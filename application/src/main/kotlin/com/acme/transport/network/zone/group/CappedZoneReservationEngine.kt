package com.acme.transport.network.zone.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped zone reservation calculation. */
interface CappedZoneReservationStep {
    fun describe(): String
    fun adjust(item: CappedZoneReservation, value: Double): Double
}

data class RoundingCappedZoneReservationAdjuster(var step: Double) : CappedZoneReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedZoneReservation, value: Double): Double = roundTo(value, step)
}

data class TieredCappedZoneReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedZoneReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedZoneReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleCappedZoneReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedZoneReservationStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: CappedZoneReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedZoneReservationAdjuster(var minimum: Double) : CappedZoneReservationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedZoneReservation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedZoneReservationAdjuster(var cap: Double) : CappedZoneReservationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedZoneReservation, value: Double): Double = minOf(value, cap)
}

data class PercentCappedZoneReservationAdjuster(var percent: Double) : CappedZoneReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedZoneReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped zone reservation steps in order and keeps a trace of each. */
data class CappedZoneReservationEngine(
    var steps: MutableList<CappedZoneReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedZoneReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedZoneReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedZoneReservationEngine(): CappedZoneReservationEngine {
    return CappedZoneReservationEngine(
        steps = mutableListOf<CappedZoneReservationStep>(
            RoundingCappedZoneReservationAdjuster(5.0),
            TieredCappedZoneReservationAdjuster(
                mutableListOf<Double>(8057.0, 8963.0),
                mutableListOf<Double>(1.14, 1.2),
            ),
            LocaleCappedZoneReservationAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "north" to 1.4, "east" to 0.91),
            ),
            MinimumCappedZoneReservationAdjuster(16.5),
            CapCappedZoneReservationAdjuster(8837.0),
            PercentCappedZoneReservationAdjuster(15.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 9806.3,
    )
}
