package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered rest break exception calculation. */
interface TieredRestBreakExceptionAdjuster {
    fun describe(): String
    fun adjust(item: TieredRestBreakException, value: Double): Double
}

data class MinimumTieredRestBreakExceptionAdjuster(var minimum: Double) : TieredRestBreakExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredRestBreakException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedTieredRestBreakExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredRestBreakExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredRestBreakException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelTieredRestBreakExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRestBreakExceptionAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredRestBreakException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered rest break exception steps in order and keeps a trace of each. */
data class TieredRestBreakExceptionEngine(
    var steps: MutableList<TieredRestBreakExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredRestBreakException): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredRestBreakException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredRestBreakExceptionEngine(flags: Registry): TieredRestBreakExceptionEngine {
    return TieredRestBreakExceptionEngine(
        steps = mutableListOf<TieredRestBreakExceptionAdjuster>(
            MinimumTieredRestBreakExceptionAdjuster(46.4),
            FlaggedTieredRestBreakExceptionAdjuster(flags, "tiered-rest-break-exception-scheduled", -2.3),
            LabelTieredRestBreakExceptionAdjuster(mutableMapOf<String, Double>("south" to 1.1, "west" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 28169.2,
    )
}
