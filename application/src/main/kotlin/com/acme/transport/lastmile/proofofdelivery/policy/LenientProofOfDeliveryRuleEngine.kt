package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient proof of delivery rule calculation. */
interface LenientProofOfDeliveryRuleStep {
    fun describe(): String
    fun adjust(item: LenientProofOfDeliveryRule, value: Double): Double
}

data class CapLenientProofOfDeliveryRuleAdjuster(var cap: Double) : LenientProofOfDeliveryRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientProofOfDeliveryRule, value: Double): Double = minOf(value, cap)
}

data class FlaggedLenientProofOfDeliveryRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientProofOfDeliveryRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientProofOfDeliveryRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LenientProofOfDeliveryRuleCapacityOffsetAdjuster(
    var weight: Double,
) : LenientProofOfDeliveryRuleStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: LenientProofOfDeliveryRule, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class RoundingLenientProofOfDeliveryRuleAdjuster(var step: Double) : LenientProofOfDeliveryRuleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientProofOfDeliveryRule, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient proof of delivery rule steps in order and keeps a trace of each. */
data class LenientProofOfDeliveryRuleEngine(
    var steps: MutableList<LenientProofOfDeliveryRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientProofOfDeliveryRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientProofOfDeliveryRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientProofOfDeliveryRuleEngine(flags: Registry): LenientProofOfDeliveryRuleEngine {
    return LenientProofOfDeliveryRuleEngine(
        steps = mutableListOf<LenientProofOfDeliveryRuleStep>(
            CapLenientProofOfDeliveryRuleAdjuster(44459.0),
            FlaggedLenientProofOfDeliveryRuleAdjuster(flags, "lenient-proof-of-delivery-rule-legacy", 1.7),
            LenientProofOfDeliveryRuleCapacityOffsetAdjuster(1.31),
            RoundingLenientProofOfDeliveryRuleAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 32979.9,
    )
}
