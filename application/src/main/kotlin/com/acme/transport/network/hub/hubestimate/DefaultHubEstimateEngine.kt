package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default hub estimate calculation. */
interface DefaultHubEstimateFactor {
    fun describe(): String
    fun adjust(item: DefaultHubEstimate, value: Double): Double
}

data class TieredDefaultHubEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultHubEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultHubEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultHubEstimateAdjuster(var minimum: Double) : DefaultHubEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultHubEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultHubEstimateAdjuster(var step: Double) : DefaultHubEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultHubEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultHubEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultHubEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultHubEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultHubEstimateAdjuster(var cap: Double) : DefaultHubEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultHubEstimate, value: Double): Double = minOf(value, cap)
}

/** Runs the default hub estimate steps in order and keeps a trace of each. */
data class DefaultHubEstimateEngine(
    var steps: MutableList<DefaultHubEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultHubEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultHubEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultHubEstimateEngine(flags: Registry): DefaultHubEstimateEngine {
    return DefaultHubEstimateEngine(
        steps = mutableListOf<DefaultHubEstimateFactor>(
            TieredDefaultHubEstimateAdjuster(
                mutableListOf<Double>(6246.0, 8982.0),
                mutableListOf<Double>(0.8, 0.83),
            ),
            MinimumDefaultHubEstimateAdjuster(14.4),
            RoundingDefaultHubEstimateAdjuster(0.25),
            FlaggedDefaultHubEstimateAdjuster(flags, "default-hub-estimate-adaptive", -5.0),
            CapDefaultHubEstimateAdjuster(28832.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 86997.4,
    )
}
