package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback hold policy calculation. */
interface FallbackHoldPolicyFactor {
    fun describe(): String
    fun adjust(item: FallbackHoldPolicy, value: Double): Double
}

data class CapFallbackHoldPolicyAdjuster(var cap: Double) : FallbackHoldPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackHoldPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackHoldPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackHoldPolicyFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackHoldPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class FallbackHoldPolicyCeilingOffsetAdjuster(var weight: Double) : FallbackHoldPolicyFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: FallbackHoldPolicy, value: Double): Double = value + item.ceiling * weight
}

/** Runs the fallback hold policy steps in order and keeps a trace of each. */
data class FallbackHoldPolicyEngine(
    var steps: MutableList<FallbackHoldPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackHoldPolicy): Double {
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

    fun evaluateAll(items: MutableList<FallbackHoldPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackHoldPolicyEngine(flags: Registry): FallbackHoldPolicyEngine {
    return FallbackHoldPolicyEngine(
        steps = mutableListOf<FallbackHoldPolicyFactor>(
            CapFallbackHoldPolicyAdjuster(18604.0),
            FlaggedFallbackHoldPolicyAdjuster(flags, "fallback-hold-policy-regional", -9.9),
            FallbackHoldPolicyCeilingOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 92105.8,
    )
}
