package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation exception calculation. */
interface SortationExceptionFactor {
    fun describe(): String
    fun adjust(item: SortationException, value: Double): Double
}

data class LaneSortationExceptionAdjuster(var factors: MutableMap<String, Double>) : SortationExceptionFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: SortationException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSortationExceptionAdjuster(var step: Double) : SortationExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationException, value: Double): Double = roundTo(value, step)
}

data class FlaggedSortationExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SortationExceptionGraceDaysOffsetAdjuster(var weight: Double) : SortationExceptionFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: SortationException, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumSortationExceptionAdjuster(var minimum: Double) : SortationExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the sortation exception steps in order and keeps a trace of each. */
data class SortationExceptionEngine(
    var steps: MutableList<SortationExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationException): Double {
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

    fun evaluateAll(items: MutableList<SortationException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationExceptionEngine(flags: Registry): SortationExceptionEngine {
    return SortationExceptionEngine(
        steps = mutableListOf<SortationExceptionFactor>(
            LaneSortationExceptionAdjuster(
                mutableMapOf<String, Double>("west" to 1.22, "north" to 0.87, "south" to 0.81),
            ),
            RoundingSortationExceptionAdjuster(1.0),
            FlaggedSortationExceptionAdjuster(flags, "sortation-exception-lenient", -9.8),
            SortationExceptionGraceDaysOffsetAdjuster(0.32),
            MinimumSortationExceptionAdjuster(10.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 97087.0,
    )
}
