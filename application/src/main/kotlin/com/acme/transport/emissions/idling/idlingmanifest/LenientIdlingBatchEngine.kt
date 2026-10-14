package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient idling batch calculation. */
interface LenientIdlingBatchModifier {
    fun describe(): String
    fun adjust(item: LenientIdlingBatch, value: Double): Double
}

data class RoundingLenientIdlingBatchAdjuster(var step: Double) : LenientIdlingBatchModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientIdlingBatchAdjuster(var minimum: Double) : LenientIdlingBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLenientIdlingBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientIdlingBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LenientIdlingBatchWindowDaysOffsetAdjuster(var weight: Double) : LenientIdlingBatchModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class PercentLenientIdlingBatchAdjuster(var percent: Double) : LenientIdlingBatchModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLenientIdlingBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientIdlingBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientIdlingBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient idling batch steps in order and keeps a trace of each. */
data class LenientIdlingBatchEngine(
    var steps: MutableList<LenientIdlingBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientIdlingBatch): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientIdlingBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientIdlingBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientIdlingBatchEngine(flags: Registry): LenientIdlingBatchEngine {
    return LenientIdlingBatchEngine(
        steps = mutableListOf<LenientIdlingBatchModifier>(
            RoundingLenientIdlingBatchAdjuster(0.1),
            MinimumLenientIdlingBatchAdjuster(30.1),
            TieredLenientIdlingBatchAdjuster(
                mutableListOf<Double>(190.0, 5133.0, 9056.0),
                mutableListOf<Double>(1.16, 0.91, 1.05),
            ),
            LenientIdlingBatchWindowDaysOffsetAdjuster(1.66),
            PercentLenientIdlingBatchAdjuster(-20.7),
            FlaggedLenientIdlingBatchAdjuster(flags, "lenient-idling-batch-legacy", 5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 54966.4,
    )
}
