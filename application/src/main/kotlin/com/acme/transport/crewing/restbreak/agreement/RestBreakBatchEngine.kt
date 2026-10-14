package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break batch calculation. */
interface RestBreakBatchFactor {
    fun describe(): String
    fun adjust(item: RestBreakBatch, value: Double): Double
}

data class RestBreakBatchAttemptsOffsetAdjuster(var weight: Double) : RestBreakBatchFactor {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RestBreakBatch, value: Double): Double = value + item.attempts.toDouble() * weight
}

data class MinimumRestBreakBatchAdjuster(var minimum: Double) : RestBreakBatchFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRestBreakBatchAdjuster(var percent: Double) : RestBreakBatchFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRestBreakBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakBatchFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRestBreakBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakBatchFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rest break batch steps in order and keeps a trace of each. */
data class RestBreakBatchEngine(
    var steps: MutableList<RestBreakBatchFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakBatch): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakBatchEngine(flags: Registry): RestBreakBatchEngine {
    return RestBreakBatchEngine(
        steps = mutableListOf<RestBreakBatchFactor>(
            RestBreakBatchAttemptsOffsetAdjuster(0.29),
            MinimumRestBreakBatchAdjuster(32.9),
            PercentRestBreakBatchAdjuster(-3.0),
            FlaggedRestBreakBatchAdjuster(flags, "rest-break-batch-scheduled", 8.7),
            TieredRestBreakBatchAdjuster(
                mutableListOf<Double>(4238.0, 5201.0),
                mutableListOf<Double>(1.04, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 46440.8,
    )
}
