package com.acme.transport.network.node.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary node profile calculation. */
interface PrimaryNodeProfileAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryNodeProfile, value: Double): Double
}

data class MinimumPrimaryNodeProfileAdjuster(var minimum: Double) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryNodeProfileAdjuster(var step: Double) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryNodeProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPrimaryNodeProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelPrimaryNodeProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryNodeProfileBudgetOffsetAdjuster(var weight: Double) : PrimaryNodeProfileAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PrimaryNodeProfile, value: Double): Double = value + item.budget * weight
}

/** Runs the primary node profile steps in order and keeps a trace of each. */
data class PrimaryNodeProfileEngine(
    var steps: MutableList<PrimaryNodeProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryNodeProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryNodeProfileAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryNodeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryNodeProfileEngine(flags: Registry): PrimaryNodeProfileEngine {
    return PrimaryNodeProfileEngine(
        steps = mutableListOf<PrimaryNodeProfileAdjuster>(
            MinimumPrimaryNodeProfileAdjuster(38.4),
            RoundingPrimaryNodeProfileAdjuster(0.1),
            TieredPrimaryNodeProfileAdjuster(
                mutableListOf<Double>(4880.0, 8110.0, 8246.0),
                mutableListOf<Double>(0.91, 0.85, 1.06),
            ),
            FlaggedPrimaryNodeProfileAdjuster(flags, "primary-node-profile-regional", -9.2),
            ChannelPrimaryNodeProfileAdjuster(
                mutableMapOf<String, Double>("north" to 1.36, "west" to 0.99, "central" to 1.05),
            ),
            PrimaryNodeProfileBudgetOffsetAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 76484.4,
    )
}
