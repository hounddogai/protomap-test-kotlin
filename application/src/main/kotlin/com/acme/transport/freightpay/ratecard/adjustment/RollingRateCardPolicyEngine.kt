package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling rate card policy calculation. */
interface RollingRateCardPolicyFactor {
    fun describe(): String
    fun adjust(item: RollingRateCardPolicy, value: Double): Double
}

data class MinimumRollingRateCardPolicyAdjuster(var minimum: Double) : RollingRateCardPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingRateCardPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRollingRateCardPolicyAdjuster(var percent: Double) : RollingRateCardPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRateCardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingRateCardPolicyAdjuster(var cap: Double) : RollingRateCardPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingRateCardPolicy, value: Double): Double = minOf(value, cap)
}

data class NameRollingRateCardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRateCardPolicyFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingRateCardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRollingRateCardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingRateCardPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingRateCardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling rate card policy steps in order and keeps a trace of each. */
data class RollingRateCardPolicyEngine(
    var steps: MutableList<RollingRateCardPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingRateCardPolicy): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingRateCardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingRateCardPolicyEngine(flags: Registry): RollingRateCardPolicyEngine {
    return RollingRateCardPolicyEngine(
        steps = mutableListOf<RollingRateCardPolicyFactor>(
            MinimumRollingRateCardPolicyAdjuster(33.5),
            PercentRollingRateCardPolicyAdjuster(-15.2),
            CapRollingRateCardPolicyAdjuster(26749.0),
            NameRollingRateCardPolicyAdjuster(mutableMapOf<String, Double>("central" to 0.8, "retail" to 1.35)),
            FlaggedRollingRateCardPolicyAdjuster(flags, "rolling-rate-card-policy-lenient", 0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 40701.0,
    )
}
