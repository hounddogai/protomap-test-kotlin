package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict rate card exception calculation. */
interface StrictRateCardExceptionAdjuster {
    fun describe(): String
    fun adjust(item: StrictRateCardException, value: Double): Double
}

data class TieredStrictRateCardExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRateCardException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictRateCardExceptionAdjuster(var step: Double) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRateCardException, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictRateCardExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRateCardException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictRateCardExceptionAdjuster(var minimum: Double) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRateCardException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentStrictRateCardExceptionAdjuster(var percent: Double) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRateCardException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictRateCardExceptionScoreOffsetAdjuster(var weight: Double) : StrictRateCardExceptionAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: StrictRateCardException, value: Double): Double = value + item.score * weight
}

/** Runs the strict rate card exception steps in order and keeps a trace of each. */
data class StrictRateCardExceptionEngine(
    var steps: MutableList<StrictRateCardExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRateCardException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictRateCardException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRateCardExceptionEngine(flags: Registry): StrictRateCardExceptionEngine {
    return StrictRateCardExceptionEngine(
        steps = mutableListOf<StrictRateCardExceptionAdjuster>(
            TieredStrictRateCardExceptionAdjuster(
                mutableListOf<Double>(3174.0, 9903.0),
                mutableListOf<Double>(1.13, 0.82),
            ),
            RoundingStrictRateCardExceptionAdjuster(0.5),
            FlaggedStrictRateCardExceptionAdjuster(flags, "strict-rate-card-exception-weighted", -6.4),
            MinimumStrictRateCardExceptionAdjuster(12.4),
            PercentStrictRateCardExceptionAdjuster(-23.9),
            StrictRateCardExceptionScoreOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 42038.7,
    )
}
