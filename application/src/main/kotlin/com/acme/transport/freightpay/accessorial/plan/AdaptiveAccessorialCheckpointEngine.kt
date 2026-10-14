package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive accessorial checkpoint calculation. */
interface AdaptiveAccessorialCheckpointAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double
}

data class PercentAdaptiveAccessorialCheckpointAdjuster(
    var percent: Double,
) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveAccessorialCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveAccessorialCheckpointAdjuster(
    var step: Double,
) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveAccessorialCheckpointAdjuster(var cap: Double) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveAccessorialCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveAccessorialCheckpointQuantityOffsetAdjuster(
    var weight: Double,
) : AdaptiveAccessorialCheckpointAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AdaptiveAccessorialCheckpoint, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the adaptive accessorial checkpoint steps in order and keeps a trace of each. */
data class AdaptiveAccessorialCheckpointEngine(
    var steps: MutableList<AdaptiveAccessorialCheckpointAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveAccessorialCheckpoint): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveAccessorialCheckpointAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveAccessorialCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveAccessorialCheckpointEngine(flags: Registry): AdaptiveAccessorialCheckpointEngine {
    return AdaptiveAccessorialCheckpointEngine(
        steps = mutableListOf<AdaptiveAccessorialCheckpointAdjuster>(
            PercentAdaptiveAccessorialCheckpointAdjuster(-2.6),
            FlaggedAdaptiveAccessorialCheckpointAdjuster(flags, "adaptive-accessorial-checkpoint-pending", -6.8),
            RoundingAdaptiveAccessorialCheckpointAdjuster(5.0),
            CapAdaptiveAccessorialCheckpointAdjuster(5561.0),
            TieredAdaptiveAccessorialCheckpointAdjuster(
                mutableListOf<Double>(4411.0, 6796.0, 8987.0),
                mutableListOf<Double>(1.03, 1.19, 1.19),
            ),
            AdaptiveAccessorialCheckpointQuantityOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 36354.1,
    )
}
