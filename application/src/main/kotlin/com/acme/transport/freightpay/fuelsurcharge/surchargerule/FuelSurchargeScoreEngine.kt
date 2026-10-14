package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge score calculation. */
interface FuelSurchargeScoreModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeScore, value: Double): Double
}

data class FlaggedFuelSurchargeScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelSurchargeScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelSurchargeScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFuelSurchargeScoreAdjuster(var cap: Double) : FuelSurchargeScoreModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeScore, value: Double): Double = minOf(value, cap)
}

data class RoundingFuelSurchargeScoreAdjuster(var step: Double) : FuelSurchargeScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeScore, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel surcharge score steps in order and keeps a trace of each. */
data class FuelSurchargeScoreEngine(
    var steps: MutableList<FuelSurchargeScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargeScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargeScoreEngine(flags: Registry): FuelSurchargeScoreEngine {
    return FuelSurchargeScoreEngine(
        steps = mutableListOf<FuelSurchargeScoreModifier>(
            FlaggedFuelSurchargeScoreAdjuster(flags, "fuel-surcharge-score-default", -2.7),
            CapFuelSurchargeScoreAdjuster(25173.0),
            RoundingFuelSurchargeScoreAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 93955.5,
    )
}
