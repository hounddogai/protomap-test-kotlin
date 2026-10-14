package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global coaching agreement calculation. */
interface GlobalCoachingAgreementAdjuster {
    fun describe(): String
    fun adjust(item: GlobalCoachingAgreement, value: Double): Double
}

data class TieredGlobalCoachingAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCoachingAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCoachingAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalCoachingAgreementAdjuster(var percent: Double) : GlobalCoachingAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCoachingAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalCoachingAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCoachingAgreementAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCoachingAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalCoachingAgreementAdjuster(var cap: Double) : GlobalCoachingAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCoachingAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the global coaching agreement steps in order and keeps a trace of each. */
data class GlobalCoachingAgreementEngine(
    var steps: MutableList<GlobalCoachingAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalCoachingAgreement): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalCoachingAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalCoachingAgreementEngine(flags: Registry): GlobalCoachingAgreementEngine {
    return GlobalCoachingAgreementEngine(
        steps = mutableListOf<GlobalCoachingAgreementAdjuster>(
            TieredGlobalCoachingAgreementAdjuster(
                mutableListOf<Double>(543.0, 1391.0, 5360.0, 6050.0),
                mutableListOf<Double>(1.05, 0.85, 0.81, 0.96),
            ),
            PercentGlobalCoachingAgreementAdjuster(22.9),
            FlaggedGlobalCoachingAgreementAdjuster(flags, "global-coaching-agreement-regional", -7.2),
            CapGlobalCoachingAgreementAdjuster(4813.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 27648.4,
    )
}
