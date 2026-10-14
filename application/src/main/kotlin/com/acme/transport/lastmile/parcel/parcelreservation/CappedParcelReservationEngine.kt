package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped parcel reservation calculation. */
interface CappedParcelReservationStep {
    fun describe(): String
    fun adjust(item: CappedParcelReservation, value: Double): Double
}

data class PercentCappedParcelReservationAdjuster(var percent: Double) : CappedParcelReservationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedParcelReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionCappedParcelReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedParcelReservationStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CappedParcelReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedParcelReservationDiscountOffsetAdjuster(var weight: Double) : CappedParcelReservationStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CappedParcelReservation, value: Double): Double = value + item.discount * weight
}

data class RoundingCappedParcelReservationAdjuster(var step: Double) : CappedParcelReservationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedParcelReservation, value: Double): Double = roundTo(value, step)
}

data class TieredCappedParcelReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedParcelReservationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedParcelReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedParcelReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedParcelReservationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedParcelReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped parcel reservation steps in order and keeps a trace of each. */
data class CappedParcelReservationEngine(
    var steps: MutableList<CappedParcelReservationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedParcelReservation): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedParcelReservationStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedParcelReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedParcelReservationEngine(flags: Registry): CappedParcelReservationEngine {
    return CappedParcelReservationEngine(
        steps = mutableListOf<CappedParcelReservationStep>(
            PercentCappedParcelReservationAdjuster(-4.2),
            RegionCappedParcelReservationAdjuster(mutableMapOf<String, Double>("south" to 1.26)),
            CappedParcelReservationDiscountOffsetAdjuster(0.86),
            RoundingCappedParcelReservationAdjuster(5.0),
            TieredCappedParcelReservationAdjuster(
                mutableListOf<Double>(1064.0, 1090.0, 7913.0),
                mutableListOf<Double>(0.82, 1.1, 0.95),
            ),
            FlaggedCappedParcelReservationAdjuster(flags, "capped-parcel-reservation-capped", 0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 68984.9,
    )
}
