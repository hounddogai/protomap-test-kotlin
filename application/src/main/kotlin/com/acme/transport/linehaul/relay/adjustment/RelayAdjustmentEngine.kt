package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the relay adjustment calculation. */
interface RelayAdjustmentModifier {
    fun describe(): String
    fun adjust(item: RelayAdjustment, value: Double): Double
}

data class TieredRelayAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRelayAdjustmentAdjuster(var cap: Double) : RelayAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayAdjustment, value: Double): Double = minOf(value, cap)
}

data class RoundingRelayAdjustmentAdjuster(var step: Double) : RelayAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RelayAdjustment, value: Double): Double = roundTo(value, step)
}

data class MinimumRelayAdjustmentAdjuster(var minimum: Double) : RelayAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RelayAdjustmentBatchSizeOffsetAdjuster(var weight: Double) : RelayAdjustmentModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RelayAdjustment, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedRelayAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the relay adjustment steps in order and keeps a trace of each. */
data class RelayAdjustmentEngine(
    var steps: MutableList<RelayAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayAdjustment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RelayAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayAdjustmentEngine(flags: Registry): RelayAdjustmentEngine {
    return RelayAdjustmentEngine(
        steps = mutableListOf<RelayAdjustmentModifier>(
            TieredRelayAdjustmentAdjuster(
                mutableListOf<Double>(767.0, 2285.0, 3724.0),
                mutableListOf<Double>(1.1, 1.19, 0.82),
            ),
            CapRelayAdjustmentAdjuster(24692.0),
            RoundingRelayAdjustmentAdjuster(0.1),
            MinimumRelayAdjustmentAdjuster(12.3),
            RelayAdjustmentBatchSizeOffsetAdjuster(1.61),
            FlaggedRelayAdjustmentAdjuster(flags, "relay-adjustment-lenient", 3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 36477.6,
    )
}
