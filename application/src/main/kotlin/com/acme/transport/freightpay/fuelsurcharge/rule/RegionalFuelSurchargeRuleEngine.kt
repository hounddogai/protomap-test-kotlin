package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.flags.Registry

/** One step of the regional fuel surcharge rule calculation. */
interface RegionalFuelSurchargeRuleFactor {
    fun describe(): String
    fun adjust(item: RegionalFuelSurchargeRule, value: Double): Double
}

data class FlaggedRegionalFuelSurchargeRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelSurchargeRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelSurchargeRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalFuelSurchargeRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelSurchargeRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelSurchargeRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalFuelSurchargeRuleMinimumOffsetAdjuster(
    var weight: Double,
) : RegionalFuelSurchargeRuleFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: RegionalFuelSurchargeRule, value: Double): Double = value + item.minimum * weight
}

data class MinimumRegionalFuelSurchargeRuleAdjuster(var minimum: Double) : RegionalFuelSurchargeRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelSurchargeRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional fuel surcharge rule steps in order and keeps a trace of each. */
data class RegionalFuelSurchargeRuleEngine(
    var steps: MutableList<RegionalFuelSurchargeRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalFuelSurchargeRule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelSurchargeRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelSurchargeRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalFuelSurchargeRuleEngine(flags: Registry): RegionalFuelSurchargeRuleEngine {
    return RegionalFuelSurchargeRuleEngine(
        steps = mutableListOf<RegionalFuelSurchargeRuleFactor>(
            FlaggedRegionalFuelSurchargeRuleAdjuster(flags, "regional-fuel-surcharge-rule-seasonal", -2.6),
            TieredRegionalFuelSurchargeRuleAdjuster(
                mutableListOf<Double>(615.0, 1003.0, 1286.0, 7236.0),
                mutableListOf<Double>(1.01, 0.92, 0.81, 1.09),
            ),
            RegionalFuelSurchargeRuleMinimumOffsetAdjuster(1.45),
            MinimumRegionalFuelSurchargeRuleAdjuster(11.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 59402.9,
    )
}
