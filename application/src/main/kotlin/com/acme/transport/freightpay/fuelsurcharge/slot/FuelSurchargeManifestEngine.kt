package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.numbers.roundTo

/** One step of the fuel surcharge manifest calculation. */
interface FuelSurchargeManifestModifier {
    fun describe(): String
    fun adjust(item: FuelSurchargeManifest, value: Double): Double
}

data class PercentFuelSurchargeManifestAdjuster(var percent: Double) : FuelSurchargeManifestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FuelSurchargeManifestWeightOffsetAdjuster(var weight: Double) : FuelSurchargeManifestModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double = value + item.weight * weight
}

data class RoundingFuelSurchargeManifestAdjuster(var step: Double) : FuelSurchargeManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double = roundTo(value, step)
}

data class LabelFuelSurchargeManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeManifestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapFuelSurchargeManifestAdjuster(var cap: Double) : FuelSurchargeManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double = minOf(value, cap)
}

data class TieredFuelSurchargeManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeManifestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fuel surcharge manifest steps in order and keeps a trace of each. */
data class FuelSurchargeManifestEngine(
    var steps: MutableList<FuelSurchargeManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelSurchargeManifest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargeManifestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelSurchargeManifestEngine(): FuelSurchargeManifestEngine {
    return FuelSurchargeManifestEngine(
        steps = mutableListOf<FuelSurchargeManifestModifier>(
            PercentFuelSurchargeManifestAdjuster(14.3),
            FuelSurchargeManifestWeightOffsetAdjuster(0.77),
            RoundingFuelSurchargeManifestAdjuster(0.5),
            LabelFuelSurchargeManifestAdjuster(mutableMapOf<String, Double>("south" to 1.28, "retail" to 1.35)),
            CapFuelSurchargeManifestAdjuster(8136.0),
            TieredFuelSurchargeManifestAdjuster(
                mutableListOf<Double>(2847.0, 3389.0),
                mutableListOf<Double>(1.05, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 92846.0,
    )
}
