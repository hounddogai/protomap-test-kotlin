package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the sortation score calculation. */
interface SortationScoreAdjuster {
    fun describe(): String
    fun adjust(item: SortationScore, value: Double): Double
}

data class CapSortationScoreAdjuster(var cap: Double) : SortationScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationScore, value: Double): Double = minOf(value, cap)
}

data class TieredSortationScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSortationScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the sortation score steps in order and keeps a trace of each. */
data class SortationScoreEngine(
    var steps: MutableList<SortationScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationScore): Double {
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

    fun evaluateAll(items: MutableList<SortationScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationScoreEngine(flags: Registry): SortationScoreEngine {
    return SortationScoreEngine(
        steps = mutableListOf<SortationScoreAdjuster>(
            CapSortationScoreAdjuster(33667.0),
            TieredSortationScoreAdjuster(
                mutableListOf<Double>(1231.0, 4514.0, 8857.0),
                mutableListOf<Double>(0.91, 1.17, 1.15),
            ),
            FlaggedSortationScoreAdjuster(flags, "sortation-score-pending", -5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 29966.2,
    )
}
