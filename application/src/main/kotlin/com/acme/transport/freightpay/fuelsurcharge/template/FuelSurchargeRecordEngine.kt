package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge record calculation. */
interface FuelSurchargeRecordModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeRecord, value: Double): Double
}

data class MinimumFuelSurchargeRecordAdjuster(var minimum: Double) : FuelSurchargeRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFuelSurchargeRecordAdjuster(var percent: Double) : FuelSurchargeRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FuelSurchargeRecordCeilingOffsetAdjuster(var weight: Double) : FuelSurchargeRecordModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double = value + item.ceiling * weight
}

data class FlaggedFuelSurchargeRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFuelSurchargeRecordAdjuster(var step: Double) : FuelSurchargeRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double = roundTo(value, step)
}

data class TieredFuelSurchargeRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel surcharge record steps in order and keeps a trace of each. */
data class FuelSurchargeRecordEngine(
    var steps: MutableList<FuelSurchargeRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelSurchargeRecord): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelSurchargeRecordEngine(flags: Registry): FuelSurchargeRecordEngine {
    return FuelSurchargeRecordEngine(
        steps = mutableListOf<FuelSurchargeRecordModifier>(
            MinimumFuelSurchargeRecordAdjuster(18.3),
            PercentFuelSurchargeRecordAdjuster(-14.3),
            FuelSurchargeRecordCeilingOffsetAdjuster(0.09),
            FlaggedFuelSurchargeRecordAdjuster(flags, "fuel-surcharge-record-tiered", -7.2),
            RoundingFuelSurchargeRecordAdjuster(0.5),
            TieredFuelSurchargeRecordAdjuster(
                mutableListOf<Double>(4924.0, 5197.0, 9452.0),
                mutableListOf<Double>(1.1, 0.91, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 38137.8,
    )
}
