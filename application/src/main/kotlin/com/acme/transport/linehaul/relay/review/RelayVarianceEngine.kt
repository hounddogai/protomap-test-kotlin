package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay variance calculation. */
interface RelayVarianceAdjuster {
    fun describe(): String
    fun adjust(item: RelayVariance, value: Double): Double
}

data class RelayVarianceRateOffsetAdjuster(var weight: Double) : RelayVarianceAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RelayVariance, value: Double): Double = value + item.rate * weight
}

data class CapRelayVarianceAdjuster(var cap: Double) : RelayVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingRelayVarianceAdjuster(var step: Double) : RelayVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayVariance, value: Double): Double = roundTo(value, step)
}

data class TieredRelayVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRelayVarianceAdjuster(var minimum: Double) : RelayVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRelayVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the relay variance steps in order and keeps a trace of each. */
data class RelayVarianceEngine(
    var steps: MutableList<RelayVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayVariance): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RelayVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayVarianceEngine(flags: Registry): RelayVarianceEngine {
    return RelayVarianceEngine(
        steps = mutableListOf<RelayVarianceAdjuster>(
            RelayVarianceRateOffsetAdjuster(0.42),
            CapRelayVarianceAdjuster(27935.0),
            RoundingRelayVarianceAdjuster(5.0),
            TieredRelayVarianceAdjuster(
                mutableListOf<Double>(897.0, 8026.0, 8652.0, 9318.0),
                mutableListOf<Double>(1.17, 1.09, 1.0, 0.92),
            ),
            MinimumRelayVarianceAdjuster(15.7),
            FlaggedRelayVarianceAdjuster(flags, "relay-variance-scheduled", -7.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 13169.2,
    )
}
