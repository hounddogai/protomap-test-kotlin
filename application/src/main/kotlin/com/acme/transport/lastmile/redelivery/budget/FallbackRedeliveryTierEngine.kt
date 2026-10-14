package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback redelivery tier calculation. */
interface FallbackRedeliveryTierFactor {
    fun describe(): String
    fun adjust(item: FallbackRedeliveryTier, value: Double): Double
}

data class MinimumFallbackRedeliveryTierAdjuster(var minimum: Double) : FallbackRedeliveryTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackRedeliveryTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackRedeliveryTierAdjuster(var cap: Double) : FallbackRedeliveryTierFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackRedeliveryTier, value: Double): Double = minOf(value, cap)
}

data class RoundingFallbackRedeliveryTierAdjuster(var step: Double) : FallbackRedeliveryTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackRedeliveryTier, value: Double): Double = roundTo(value, step)
}

data class DepotFallbackRedeliveryTierAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackRedeliveryTierFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackRedeliveryTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackRedeliveryTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackRedeliveryTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackRedeliveryTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the fallback redelivery tier steps in order and keeps a trace of each. */
data class FallbackRedeliveryTierEngine(
    var steps: MutableList<FallbackRedeliveryTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackRedeliveryTier): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackRedeliveryTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackRedeliveryTierEngine(flags: Registry): FallbackRedeliveryTierEngine {
    return FallbackRedeliveryTierEngine(
        steps = mutableListOf<FallbackRedeliveryTierFactor>(
            MinimumFallbackRedeliveryTierAdjuster(36.6),
            CapFallbackRedeliveryTierAdjuster(49059.0),
            RoundingFallbackRedeliveryTierAdjuster(1.0),
            DepotFallbackRedeliveryTierAdjuster(mutableMapOf<String, Double>("east" to 1.19)),
            FlaggedFallbackRedeliveryTierAdjuster(flags, "fallback-redelivery-tier-lenient", -3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 26192.5,
    )
}
