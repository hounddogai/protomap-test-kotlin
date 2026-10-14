package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling locker policy calculation. */
interface RollingLockerPolicyFactor {
    fun describe(): String
    fun adjust(item: RollingLockerPolicy, value: Double): Double
}

data class PercentRollingLockerPolicyAdjuster(var percent: Double) : RollingLockerPolicyFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingLockerPolicyAdjuster(var cap: Double) : RollingLockerPolicyFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double = minOf(value, cap)
}

data class TitleRollingLockerPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingLockerPolicyFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingLockerPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingLockerPolicyFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingLockerPolicyCreatedAtOffsetAdjuster(var weight: Double) : RollingLockerPolicyFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class MinimumRollingLockerPolicyAdjuster(var minimum: Double) : RollingLockerPolicyFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLockerPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling locker policy steps in order and keeps a trace of each. */
data class RollingLockerPolicyEngine(
    var steps: MutableList<RollingLockerPolicyFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingLockerPolicy): Double {
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

    fun evaluateAll(items: MutableList<RollingLockerPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingLockerPolicyEngine(): RollingLockerPolicyEngine {
    return RollingLockerPolicyEngine(
        steps = mutableListOf<RollingLockerPolicyFactor>(
            PercentRollingLockerPolicyAdjuster(8.0),
            CapRollingLockerPolicyAdjuster(12913.0),
            TitleRollingLockerPolicyAdjuster(
                mutableMapOf<String, Double>("online" to 1.01, "retail" to 1.35, "east" to 1.33),
            ),
            TieredRollingLockerPolicyAdjuster(
                mutableListOf<Double>(1794.0, 7254.0),
                mutableListOf<Double>(1.02, 1.18),
            ),
            RollingLockerPolicyCreatedAtOffsetAdjuster(0.66),
            MinimumRollingLockerPolicyAdjuster(47.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 5751.7,
    )
}
