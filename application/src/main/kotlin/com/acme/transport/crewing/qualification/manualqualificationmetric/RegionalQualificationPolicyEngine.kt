package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional qualification policy calculation. */
interface RegionalQualificationPolicyAdjuster {
    fun describe(): String
    fun adjust(item: RegionalQualificationPolicy, value: Double): Double
}

data class TieredRegionalQualificationPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalQualificationPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalQualificationPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalQualificationPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalQualificationPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalQualificationPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalQualificationPolicyAdjuster(var step: Double) : RegionalQualificationPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalQualificationPolicy, value: Double): Double = roundTo(value, step)
}

data class NameRegionalQualificationPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationPolicyAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalQualificationPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional qualification policy steps in order and keeps a trace of each. */
data class RegionalQualificationPolicyEngine(
    var steps: MutableList<RegionalQualificationPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalQualificationPolicy): Double {
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

    fun evaluateAll(items: MutableList<RegionalQualificationPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalQualificationPolicyEngine(flags: Registry): RegionalQualificationPolicyEngine {
    return RegionalQualificationPolicyEngine(
        steps = mutableListOf<RegionalQualificationPolicyAdjuster>(
            TieredRegionalQualificationPolicyAdjuster(
                mutableListOf<Double>(4456.0, 6301.0, 6755.0),
                mutableListOf<Double>(0.94, 1.01, 1.09),
            ),
            FlaggedRegionalQualificationPolicyAdjuster(flags, "regional-qualification-policy-primary", -0.2),
            RoundingRegionalQualificationPolicyAdjuster(0.25),
            NameRegionalQualificationPolicyAdjuster(mutableMapOf<String, Double>("east" to 1.24)),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 28653.7,
    )
}
