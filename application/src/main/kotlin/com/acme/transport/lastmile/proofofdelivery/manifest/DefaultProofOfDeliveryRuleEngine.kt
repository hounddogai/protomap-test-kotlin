package com.acme.transport.lastmile.proofofdelivery.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default proof of delivery rule calculation. */
interface DefaultProofOfDeliveryRuleStep {
    fun describe(): String
    fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double
}

data class FlaggedDefaultProofOfDeliveryRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefaultProofOfDeliveryRuleAdjuster(var step: Double) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultProofOfDeliveryRuleAdjuster(var percent: Double) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultProofOfDeliveryRuleAdjuster(var cap: Double) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double = minOf(value, cap)
}

data class DefaultProofOfDeliveryRuleRevisionOffsetAdjuster(
    var weight: Double,
) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class TieredDefaultProofOfDeliveryRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultProofOfDeliveryRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultProofOfDeliveryRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default proof of delivery rule steps in order and keeps a trace of each. */
data class DefaultProofOfDeliveryRuleEngine(
    var steps: MutableList<DefaultProofOfDeliveryRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultProofOfDeliveryRule): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultProofOfDeliveryRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultProofOfDeliveryRuleEngine(flags: Registry): DefaultProofOfDeliveryRuleEngine {
    return DefaultProofOfDeliveryRuleEngine(
        steps = mutableListOf<DefaultProofOfDeliveryRuleStep>(
            FlaggedDefaultProofOfDeliveryRuleAdjuster(flags, "default-proof-of-delivery-rule-strict", -5.3),
            RoundingDefaultProofOfDeliveryRuleAdjuster(0.25),
            PercentDefaultProofOfDeliveryRuleAdjuster(13.6),
            CapDefaultProofOfDeliveryRuleAdjuster(15593.0),
            DefaultProofOfDeliveryRuleRevisionOffsetAdjuster(1.27),
            TieredDefaultProofOfDeliveryRuleAdjuster(
                mutableListOf<Double>(4403.0, 4647.0),
                mutableListOf<Double>(1.02, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 13969.1,
    )
}
