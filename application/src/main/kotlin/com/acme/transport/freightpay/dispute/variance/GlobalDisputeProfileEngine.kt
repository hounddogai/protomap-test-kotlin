package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global dispute profile calculation. */
interface GlobalDisputeProfileStep {
    fun describe(): String
    fun adjust(item: GlobalDisputeProfile, value: Double): Double
}

data class FlaggedGlobalDisputeProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalDisputeProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalDisputeProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalDisputeProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalDisputeProfileStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalDisputeProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class GlobalDisputeProfileLimitOffsetAdjuster(var weight: Double) : GlobalDisputeProfileStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: GlobalDisputeProfile, value: Double): Double = value + item.limit.toDouble() * weight
}

data class PercentGlobalDisputeProfileAdjuster(var percent: Double) : GlobalDisputeProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalDisputeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumGlobalDisputeProfileAdjuster(var minimum: Double) : GlobalDisputeProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalDisputeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global dispute profile steps in order and keeps a trace of each. */
data class GlobalDisputeProfileEngine(
    var steps: MutableList<GlobalDisputeProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalDisputeProfile): Double {
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

    fun evaluateAll(items: MutableList<GlobalDisputeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalDisputeProfileEngine(flags: Registry): GlobalDisputeProfileEngine {
    return GlobalDisputeProfileEngine(
        steps = mutableListOf<GlobalDisputeProfileStep>(
            FlaggedGlobalDisputeProfileAdjuster(flags, "global-dispute-profile-weighted", 9.0),
            TieredGlobalDisputeProfileAdjuster(
                mutableListOf<Double>(2227.0, 2867.0, 5257.0),
                mutableListOf<Double>(0.87, 1.0, 1.07),
            ),
            GlobalDisputeProfileLimitOffsetAdjuster(1.58),
            PercentGlobalDisputeProfileAdjuster(-14.0),
            MinimumGlobalDisputeProfileAdjuster(29.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 55079.4,
    )
}
