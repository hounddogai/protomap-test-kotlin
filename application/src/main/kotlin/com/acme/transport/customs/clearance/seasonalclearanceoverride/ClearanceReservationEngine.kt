package com.acme.transport.customs.clearance.seasonalclearanceoverride

/** One step of the clearance reservation calculation. */
interface ClearanceReservationAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceReservation, value: Double): Double
}

data class TieredClearanceReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ClearanceReservationWindowDaysOffsetAdjuster(var weight: Double) : ClearanceReservationAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: ClearanceReservation, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class CapClearanceReservationAdjuster(var cap: Double) : ClearanceReservationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the clearance reservation steps in order and keeps a trace of each. */
data class ClearanceReservationEngine(
    var steps: MutableList<ClearanceReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceReservation): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceReservationEngine(): ClearanceReservationEngine {
    return ClearanceReservationEngine(
        steps = mutableListOf<ClearanceReservationAdjuster>(
            TieredClearanceReservationAdjuster(
                mutableListOf<Double>(1744.0, 2723.0, 5019.0, 8529.0),
                mutableListOf<Double>(1.14, 0.82, 1.03, 0.85),
            ),
            ClearanceReservationWindowDaysOffsetAdjuster(0.93),
            CapClearanceReservationAdjuster(13058.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 95501.0,
    )
}
