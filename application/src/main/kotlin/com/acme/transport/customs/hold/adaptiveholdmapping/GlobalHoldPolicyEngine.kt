package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.kit.flags.Registry

/** One step of the global hold policy calculation. */
interface GlobalHoldPolicyAdjuster {
    fun describe(): String
    fun adjust(item: GlobalHoldPolicy, value: Double): Double
}

data class GlobalHoldPolicyFloorValueOffsetAdjuster(var weight: Double) : GlobalHoldPolicyAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: GlobalHoldPolicy, value: Double): Double = value + item.floorValue * weight
}

data class MinimumGlobalHoldPolicyAdjuster(var minimum: Double) : GlobalHoldPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalHoldPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalHoldPolicyAdjuster(var percent: Double) : GlobalHoldPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalHoldPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalHoldPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalHoldPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalHoldPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalHoldPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalHoldPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalHoldPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global hold policy steps in order and keeps a trace of each. */
data class GlobalHoldPolicyEngine(
    var steps: MutableList<GlobalHoldPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalHoldPolicy): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalHoldPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalHoldPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalHoldPolicyEngine(flags: Registry): GlobalHoldPolicyEngine {
    return GlobalHoldPolicyEngine(
        steps = mutableListOf<GlobalHoldPolicyAdjuster>(
            GlobalHoldPolicyFloorValueOffsetAdjuster(1.3),
            MinimumGlobalHoldPolicyAdjuster(35.0),
            PercentGlobalHoldPolicyAdjuster(0.9),
            TieredGlobalHoldPolicyAdjuster(
                mutableListOf<Double>(1670.0, 2151.0, 4531.0, 7788.0),
                mutableListOf<Double>(0.96, 1.12, 1.16, 1.01),
            ),
            FlaggedGlobalHoldPolicyAdjuster(flags, "global-hold-policy-default", -2.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 41983.3,
    )
}
