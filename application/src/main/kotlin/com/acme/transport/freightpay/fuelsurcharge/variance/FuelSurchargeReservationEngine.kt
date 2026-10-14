package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge reservation calculation. */
interface FuelSurchargeReservationAdjuster {
    fun describe(): String
    fun adjust(item: FuelSurchargeReservation, value: Double): Double
}

data class RoundingFuelSurchargeReservationAdjuster(var step: Double) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double = roundTo(value, step)
}

data class PercentFuelSurchargeReservationAdjuster(var percent: Double) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFuelSurchargeReservationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FuelSurchargeReservationAttemptsOffsetAdjuster(
    var weight: Double,
) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class NameFuelSurchargeReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredFuelSurchargeReservationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeReservationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeReservation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel surcharge reservation steps in order and keeps a trace of each. */
data class FuelSurchargeReservationEngine(
    var steps: MutableList<FuelSurchargeReservationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeReservationAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeReservationEngine(flags: Registry): FuelSurchargeReservationEngine {
    return FuelSurchargeReservationEngine(
        steps = mutableListOf<FuelSurchargeReservationAdjuster>(
            RoundingFuelSurchargeReservationAdjuster(1.0),
            PercentFuelSurchargeReservationAdjuster(-12.9),
            FlaggedFuelSurchargeReservationAdjuster(flags, "fuel-surcharge-reservation-legacy", 0.0),
            FuelSurchargeReservationAttemptsOffsetAdjuster(1.99),
            NameFuelSurchargeReservationAdjuster(mutableMapOf<String, Double>("west" to 1.01)),
            TieredFuelSurchargeReservationAdjuster(
                mutableListOf<Double>(4999.0, 6080.0, 6601.0, 9508.0),
                mutableListOf<Double>(1.03, 0.99, 0.93, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 7267.4,
    )
}
