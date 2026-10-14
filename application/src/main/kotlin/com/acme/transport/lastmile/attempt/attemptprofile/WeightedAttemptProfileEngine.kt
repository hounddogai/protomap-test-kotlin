package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted attempt profile calculation. */
interface WeightedAttemptProfileFactor {
    fun describe(): String
    fun adjust(item: WeightedAttemptProfile, value: Double): Double
}

data class TieredWeightedAttemptProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAttemptProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedAttemptProfileAdjuster(var cap: Double) : WeightedAttemptProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double = minOf(value, cap)
}

data class WeightedAttemptProfileQuantityOffsetAdjuster(var weight: Double) : WeightedAttemptProfileFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class FlaggedWeightedAttemptProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAttemptProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SegmentWeightedAttemptProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAttemptProfileFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedAttemptProfileAdjuster(var step: Double) : WeightedAttemptProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAttemptProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted attempt profile steps in order and keeps a trace of each. */
data class WeightedAttemptProfileEngine(
    var steps: MutableList<WeightedAttemptProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedAttemptProfile): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedAttemptProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedAttemptProfileEngine(flags: Registry): WeightedAttemptProfileEngine {
    return WeightedAttemptProfileEngine(
        steps = mutableListOf<WeightedAttemptProfileFactor>(
            TieredWeightedAttemptProfileAdjuster(
                mutableListOf<Double>(893.0, 4183.0),
                mutableListOf<Double>(0.83, 0.93),
            ),
            CapWeightedAttemptProfileAdjuster(31083.0),
            WeightedAttemptProfileQuantityOffsetAdjuster(0.96),
            FlaggedWeightedAttemptProfileAdjuster(flags, "weighted-attempt-profile-fallback", 3.1),
            SegmentWeightedAttemptProfileAdjuster(mutableMapOf<String, Double>("online" to 0.91)),
            RoundingWeightedAttemptProfileAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 98711.9,
    )
}
