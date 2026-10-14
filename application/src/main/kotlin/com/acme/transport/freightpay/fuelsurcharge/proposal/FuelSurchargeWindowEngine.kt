package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge window calculation. */
interface FuelSurchargeWindowStep {
    fun describe(): String
    fun adjust(item: FuelSurchargeWindow, value: Double): Double
}

data class FlaggedFuelSurchargeWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingFuelSurchargeWindowAdjuster(var step: Double) : FuelSurchargeWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeWindow, value: Double): Double = roundTo(value, step)
}

data class FuelSurchargeWindowStartsAtOffsetAdjuster(var weight: Double) : FuelSurchargeWindowStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: FuelSurchargeWindow, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class CapFuelSurchargeWindowAdjuster(var cap: Double) : FuelSurchargeWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeWindow, value: Double): Double = minOf(value, cap)
}

data class CountryFuelSurchargeWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeWindowStep {
    override fun describe(): String = "by country"

    override fun adjust(item: FuelSurchargeWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel surcharge window steps in order and keeps a trace of each. */
data class FuelSurchargeWindowEngine(
    var steps: MutableList<FuelSurchargeWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelSurchargeWindow): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelSurchargeWindowEngine(flags: Registry): FuelSurchargeWindowEngine {
    return FuelSurchargeWindowEngine(
        steps = mutableListOf<FuelSurchargeWindowStep>(
            FlaggedFuelSurchargeWindowAdjuster(flags, "fuel-surcharge-window-legacy", 7.6),
            RoundingFuelSurchargeWindowAdjuster(1.0),
            FuelSurchargeWindowStartsAtOffsetAdjuster(0.7),
            CapFuelSurchargeWindowAdjuster(25127.0),
            CountryFuelSurchargeWindowAdjuster(mutableMapOf<String, Double>("east" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 92744.2,
    )
}
