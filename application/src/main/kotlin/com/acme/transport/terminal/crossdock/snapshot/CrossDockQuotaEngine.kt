package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock quota calculation. */
interface CrossDockQuotaModifier {
    fun describe(): String
    fun adjust(item: CrossDockQuota, value: Double): Double
}

data class CrossDockQuotaWeightOffsetAdjuster(var weight: Double) : CrossDockQuotaModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: CrossDockQuota, value: Double): Double = value + item.weight * weight
}

data class MinimumCrossDockQuotaAdjuster(var minimum: Double) : CrossDockQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCrossDockQuotaAdjuster(var step: Double) : CrossDockQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedCrossDockQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCrossDockQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cross dock quota steps in order and keeps a trace of each. */
data class CrossDockQuotaEngine(
    var steps: MutableList<CrossDockQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CrossDockQuota): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCrossDockQuotaEngine(flags: Registry): CrossDockQuotaEngine {
    return CrossDockQuotaEngine(
        steps = mutableListOf<CrossDockQuotaModifier>(
            CrossDockQuotaWeightOffsetAdjuster(0.05),
            MinimumCrossDockQuotaAdjuster(42.5),
            RoundingCrossDockQuotaAdjuster(0.05),
            FlaggedCrossDockQuotaAdjuster(flags, "cross-dock-quota-seasonal", -8.3),
            TieredCrossDockQuotaAdjuster(
                mutableListOf<Double>(3163.0, 3600.0, 5615.0, 6663.0),
                mutableListOf<Double>(0.98, 1.14, 0.82, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 42940.3,
    )
}
