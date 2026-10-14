package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling accessorial policy calculation. */
interface RollingAccessorialPolicyStep {
    fun describe(): String
    fun adjust(item: RollingAccessorialPolicy, value: Double): Double
}

data class FlaggedRollingAccessorialPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingAccessorialPolicyStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingAccessorialPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRollingAccessorialPolicyAdjuster(var percent: Double) : RollingAccessorialPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingAccessorialPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingAccessorialPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingAccessorialPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingAccessorialPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rolling accessorial policy steps in order and keeps a trace of each. */
data class RollingAccessorialPolicyEngine(
    var steps: MutableList<RollingAccessorialPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingAccessorialPolicy): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingAccessorialPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingAccessorialPolicyEngine(flags: Registry): RollingAccessorialPolicyEngine {
    return RollingAccessorialPolicyEngine(
        steps = mutableListOf<RollingAccessorialPolicyStep>(
            FlaggedRollingAccessorialPolicyAdjuster(flags, "rolling-accessorial-policy-tiered", 0.3),
            PercentRollingAccessorialPolicyAdjuster(-20.9),
            TieredRollingAccessorialPolicyAdjuster(
                mutableListOf<Double>(6131.0, 6622.0, 7301.0),
                mutableListOf<Double>(1.12, 0.98, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 57572.7,
    )
}
