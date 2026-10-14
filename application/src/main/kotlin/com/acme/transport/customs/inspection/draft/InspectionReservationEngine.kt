package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection reservation calculation. */
interface InspectionReservationFactor {
    fun describe(): String
    fun adjust(item: InspectionReservation, value: Double): Double
}

data class InspectionReservationRetriesOffsetAdjuster(var weight: Double) : InspectionReservationFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: InspectionReservation, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TieredInspectionReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionReservationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapInspectionReservationAdjuster(var cap: Double) : InspectionReservationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionReservation, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionReservationAdjuster(var step: Double) : InspectionReservationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionReservation, value: Double): Double = roundTo(value, step)
}

data class LabelInspectionReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionReservationFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection reservation steps in order and keeps a trace of each. */
data class InspectionReservationEngine(
    var steps: MutableList<InspectionReservationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionReservation): Double {
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

    fun evaluateAll(items: MutableList<InspectionReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionReservationEngine(): InspectionReservationEngine {
    return InspectionReservationEngine(
        steps = mutableListOf<InspectionReservationFactor>(
            InspectionReservationRetriesOffsetAdjuster(1.31),
            TieredInspectionReservationAdjuster(
                mutableListOf<Double>(5698.0, 7169.0),
                mutableListOf<Double>(1.18, 1.03),
            ),
            CapInspectionReservationAdjuster(26526.0),
            RoundingInspectionReservationAdjuster(0.5),
            LabelInspectionReservationAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "east" to 1.13, "north" to 1.38),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 75321.7,
    )
}
