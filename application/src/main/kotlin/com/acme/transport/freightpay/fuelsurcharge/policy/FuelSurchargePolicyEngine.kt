package com.acme.transport.freightpay.fuelsurcharge.policy

/** One step of the fuel surcharge policy calculation. */
interface FuelSurchargePolicyStep {
    fun describe(): String
    fun adjust(item: FuelSurchargePolicy, value: Double): Double
}

data class TieredFuelSurchargePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceFuelSurchargePolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargePolicyStep {
    override fun describe(): String = "by source"

    override fun adjust(item: FuelSurchargePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumFuelSurchargePolicyAdjuster(var minimum: Double) : FuelSurchargePolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargePolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the fuel surcharge policy steps in order and keeps a trace of each. */
data class FuelSurchargePolicyEngine(
    var steps: MutableList<FuelSurchargePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FuelSurchargePolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FuelSurchargePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<FuelSurchargePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFuelSurchargePolicyEngine(): FuelSurchargePolicyEngine {
    return FuelSurchargePolicyEngine(
        steps = mutableListOf<FuelSurchargePolicyStep>(
            TieredFuelSurchargePolicyAdjuster(
                mutableListOf<Double>(3261.0, 4088.0, 4510.0, 8068.0),
                mutableListOf<Double>(1.01, 0.89, 0.86, 0.88),
            ),
            SourceFuelSurchargePolicyAdjuster(mutableMapOf<String, Double>("online" to 0.96)),
            MinimumFuelSurchargePolicyAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 59588.0,
    )
}
