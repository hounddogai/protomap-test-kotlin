package com.acme.transport.freightpay.fuelsurcharge.milestone

/** One step of the regional fuel surcharge group calculation. */
interface RegionalFuelSurchargeGroupModifier {
    fun describe(): String
    fun adjust(item: RegionalFuelSurchargeGroup, value: Double): Double
}

data class RegionalFuelSurchargeGroupPriorityOffsetAdjuster(
    var weight: Double,
) : RegionalFuelSurchargeGroupModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RegionalFuelSurchargeGroup, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class PercentRegionalFuelSurchargeGroupAdjuster(var percent: Double) : RegionalFuelSurchargeGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFuelSurchargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalFuelSurchargeGroupAdjuster(var minimum: Double) : RegionalFuelSurchargeGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelSurchargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional fuel surcharge group steps in order and keeps a trace of each. */
data class RegionalFuelSurchargeGroupEngine(
    var steps: MutableList<RegionalFuelSurchargeGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalFuelSurchargeGroup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelSurchargeGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelSurchargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalFuelSurchargeGroupEngine(): RegionalFuelSurchargeGroupEngine {
    return RegionalFuelSurchargeGroupEngine(
        steps = mutableListOf<RegionalFuelSurchargeGroupModifier>(
            RegionalFuelSurchargeGroupPriorityOffsetAdjuster(0.89),
            PercentRegionalFuelSurchargeGroupAdjuster(7.2),
            MinimumRegionalFuelSurchargeGroupAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 23479.7,
    )
}
