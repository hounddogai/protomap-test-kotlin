package com.acme.transport.customs.hold.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the hold policy calculation. */
interface HoldPolicyFactor {
    fun describe(): String
    fun adjust(item: HoldPolicy, value: Double): Double
}

data class TieredHoldPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHoldPolicyAdjuster(var percent: Double) : HoldPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingHoldPolicyAdjuster(var step: Double) : HoldPolicyFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldPolicy, value: Double): Double = roundTo(value, step)
}

data class SourceHoldPolicyAdjuster(var factors: MutableMap<String, Double>) : HoldPolicyFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: HoldPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHoldPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hold policy steps in order and keeps a trace of each. */
data class HoldPolicyEngine(
    var steps: MutableList<HoldPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldPolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldPolicyFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldPolicyEngine(flags: Registry): HoldPolicyEngine {
    return HoldPolicyEngine(
        steps = mutableListOf<HoldPolicyFactor>(
            TieredHoldPolicyAdjuster(mutableListOf<Double>(721.0, 3675.0), mutableListOf<Double>(0.96, 0.91)),
            PercentHoldPolicyAdjuster(8.5),
            RoundingHoldPolicyAdjuster(0.1),
            SourceHoldPolicyAdjuster(mutableMapOf<String, Double>("retail" to 0.95)),
            FlaggedHoldPolicyAdjuster(flags, "hold-policy-pending", -1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 86868.9,
    )
}
