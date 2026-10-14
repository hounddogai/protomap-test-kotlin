package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster reservation calculation. */
interface RosterReservationFactor {
    fun describe(): String
    fun adjust(item: RosterReservation, value: Double): Double
}

data class RosterReservationRetriesOffsetAdjuster(var weight: Double) : RosterReservationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RosterReservation, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TieredRosterReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRosterReservationAdjuster(var step: Double) : RosterReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterReservation, value: Double): Double = roundTo(value, step)
}

data class DepotRosterReservationAdjuster(var factors: MutableMap<String, Double>) : RosterReservationFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: RosterReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the roster reservation steps in order and keeps a trace of each. */
data class RosterReservationEngine(
    var steps: MutableList<RosterReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterReservation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterReservationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterReservationEngine(): RosterReservationEngine {
    return RosterReservationEngine(
        steps = mutableListOf<RosterReservationFactor>(
            RosterReservationRetriesOffsetAdjuster(1.26),
            TieredRosterReservationAdjuster(
                mutableListOf<Double>(2714.0, 2828.0, 2921.0, 8127.0),
                mutableListOf<Double>(1.17, 0.81, 1.07, 0.87),
            ),
            RoundingRosterReservationAdjuster(0.5),
            DepotRosterReservationAdjuster(mutableMapOf<String, Double>("south" to 1.07, "west" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 81785.2,
    )
}
