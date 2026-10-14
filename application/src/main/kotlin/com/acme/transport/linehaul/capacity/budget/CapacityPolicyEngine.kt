package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity policy calculation. */
interface CapacityPolicyAdjuster {
    fun describe(): String
    fun adjust(item: CapacityPolicy, value: Double): Double
}

data class MinimumCapacityPolicyAdjuster(var minimum: Double) : CapacityPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCapacityPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCapacityPolicyAdjuster(var step: Double) : CapacityPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityPolicy, value: Double): Double = roundTo(value, step)
}

data class ChannelCapacityPolicyAdjuster(var factors: MutableMap<String, Double>) : CapacityPolicyAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: CapacityPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCapacityPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capacity policy steps in order and keeps a trace of each. */
data class CapacityPolicyEngine(
    var steps: MutableList<CapacityPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityPolicy): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityPolicyEngine(flags: Registry): CapacityPolicyEngine {
    return CapacityPolicyEngine(
        steps = mutableListOf<CapacityPolicyAdjuster>(
            MinimumCapacityPolicyAdjuster(25.4),
            FlaggedCapacityPolicyAdjuster(flags, "capacity-policy-scheduled", 0.5),
            RoundingCapacityPolicyAdjuster(1.0),
            ChannelCapacityPolicyAdjuster(mutableMapOf<String, Double>("central" to 1.29)),
            TieredCapacityPolicyAdjuster(
                mutableListOf<Double>(4148.0, 5723.0),
                mutableListOf<Double>(1.16, 1.1),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 3740.9,
    )
}
