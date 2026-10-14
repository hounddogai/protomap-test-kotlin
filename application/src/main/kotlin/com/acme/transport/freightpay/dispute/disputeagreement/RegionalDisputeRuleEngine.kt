package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional dispute rule calculation. */
interface RegionalDisputeRuleFactor {
    fun describe(): String
    fun adjust(item: RegionalDisputeRule, value: Double): Double
}

data class FlaggedRegionalDisputeRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalDisputeRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalDisputeRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalDisputeRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalDisputeRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalDisputeRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalDisputeRuleAdjuster(var minimum: Double) : RegionalDisputeRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalDisputeRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional dispute rule steps in order and keeps a trace of each. */
data class RegionalDisputeRuleEngine(
    var steps: MutableList<RegionalDisputeRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalDisputeRule): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalDisputeRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalDisputeRuleEngine(flags: Registry): RegionalDisputeRuleEngine {
    return RegionalDisputeRuleEngine(
        steps = mutableListOf<RegionalDisputeRuleFactor>(
            FlaggedRegionalDisputeRuleAdjuster(flags, "regional-dispute-rule-tiered", 9.1),
            TieredRegionalDisputeRuleAdjuster(
                mutableListOf<Double>(851.0, 9140.0),
                mutableListOf<Double>(0.96, 1.1),
            ),
            MinimumRegionalDisputeRuleAdjuster(11.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 77000.5,
    )
}
