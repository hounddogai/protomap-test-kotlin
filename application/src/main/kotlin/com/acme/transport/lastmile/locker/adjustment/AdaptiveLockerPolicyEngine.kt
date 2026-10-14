package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive locker policy calculation. */
interface AdaptiveLockerPolicyStep {
    fun describe(): String
    fun adjust(item: AdaptiveLockerPolicy, value: Double): Double
}

data class FlaggedAdaptiveLockerPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLockerPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLockerPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveLockerPolicyAdjuster(var minimum: Double) : AdaptiveLockerPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLockerPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveLockerPolicyAdjuster(var percent: Double) : AdaptiveLockerPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveLockerPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameAdaptiveLockerPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLockerPolicyStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveLockerPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAdaptiveLockerPolicyAdjuster(var cap: Double) : AdaptiveLockerPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLockerPolicy, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive locker policy steps in order and keeps a trace of each. */
data class AdaptiveLockerPolicyEngine(
    var steps: MutableList<AdaptiveLockerPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveLockerPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLockerPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveLockerPolicyEngine(flags: Registry): AdaptiveLockerPolicyEngine {
    return AdaptiveLockerPolicyEngine(
        steps = mutableListOf<AdaptiveLockerPolicyStep>(
            FlaggedAdaptiveLockerPolicyAdjuster(flags, "adaptive-locker-policy-adaptive", -6.8),
            MinimumAdaptiveLockerPolicyAdjuster(18.2),
            PercentAdaptiveLockerPolicyAdjuster(-9.8),
            NameAdaptiveLockerPolicyAdjuster(mutableMapOf<String, Double>("retail" to 1.12, "central" to 1.21)),
            CapAdaptiveLockerPolicyAdjuster(11160.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 94590.7,
    )
}
