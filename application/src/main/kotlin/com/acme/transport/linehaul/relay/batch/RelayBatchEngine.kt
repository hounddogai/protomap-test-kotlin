package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay batch calculation. */
interface RelayBatchStep {
    fun describe(): String
    fun adjust(item: RelayBatch, value: Double): Double
}

data class CountryRelayBatchAdjuster(var factors: MutableMap<String, Double>) : RelayBatchStep {
    override fun describe(): String = "by country"

    override fun adjust(item: RelayBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRelayBatchAdjuster(var cap: Double) : RelayBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayBatch, value: Double): Double = minOf(value, cap)
}

data class RoundingRelayBatchAdjuster(var step: Double) : RelayBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayBatch, value: Double): Double = roundTo(value, step)
}

data class FlaggedRelayBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRelayBatchAdjuster(var minimum: Double) : RelayBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the relay batch steps in order and keeps a trace of each. */
data class RelayBatchEngine(
    var steps: MutableList<RelayBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RelayBatch): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<RelayBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRelayBatchEngine(flags: Registry): RelayBatchEngine {
    return RelayBatchEngine(
        steps = mutableListOf<RelayBatchStep>(
            CountryRelayBatchAdjuster(mutableMapOf<String, Double>("west" to 1.11, "north" to 0.86)),
            TieredRelayBatchAdjuster(
                mutableListOf<Double>(237.0, 281.0, 955.0),
                mutableListOf<Double>(1.11, 1.18, 1.14),
            ),
            CapRelayBatchAdjuster(7103.0),
            RoundingRelayBatchAdjuster(1.0),
            FlaggedRelayBatchAdjuster(flags, "relay-batch-adaptive", 4.1),
            MinimumRelayBatchAdjuster(48.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 89873.5,
    )
}
