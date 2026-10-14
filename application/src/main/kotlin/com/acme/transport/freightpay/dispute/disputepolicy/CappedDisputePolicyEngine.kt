package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped dispute policy calculation. */
interface CappedDisputePolicyStep {
    fun describe(): String
    fun adjust(item: CappedDisputePolicy, value: Double): Double
}

data class PercentCappedDisputePolicyAdjuster(var percent: Double) : CappedDisputePolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedDisputePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCappedDisputePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDisputePolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDisputePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedDisputePolicyAdjuster(var step: Double) : CappedDisputePolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedDisputePolicy, value: Double): Double = roundTo(value, step)
}

data class TieredCappedDisputePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDisputePolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDisputePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleCappedDisputePolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedDisputePolicyStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: CappedDisputePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped dispute policy steps in order and keeps a trace of each. */
data class CappedDisputePolicyEngine(
    var steps: MutableList<CappedDisputePolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedDisputePolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedDisputePolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedDisputePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedDisputePolicyEngine(flags: Registry): CappedDisputePolicyEngine {
    return CappedDisputePolicyEngine(
        steps = mutableListOf<CappedDisputePolicyStep>(
            PercentCappedDisputePolicyAdjuster(20.4),
            FlaggedCappedDisputePolicyAdjuster(flags, "capped-dispute-policy-legacy", -6.7),
            RoundingCappedDisputePolicyAdjuster(0.1),
            TieredCappedDisputePolicyAdjuster(
                mutableListOf<Double>(717.0, 880.0, 3307.0, 5533.0),
                mutableListOf<Double>(0.8, 0.81, 0.92, 0.81),
            ),
            LocaleCappedDisputePolicyAdjuster(
                mutableMapOf<String, Double>("north" to 1.07, "west" to 1.26, "central" to 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 52703.9,
    )
}
