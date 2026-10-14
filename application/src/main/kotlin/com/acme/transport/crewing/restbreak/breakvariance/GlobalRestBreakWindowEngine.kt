package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global rest break window calculation. */
interface GlobalRestBreakWindowFactor {
    fun describe(): String
    fun adjust(item: GlobalRestBreakWindow, value: Double): Double
}

data class TitleGlobalRestBreakWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRestBreakWindowFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: GlobalRestBreakWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredGlobalRestBreakWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRestBreakWindowFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRestBreakWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalRestBreakWindowAdjuster(var minimum: Double) : GlobalRestBreakWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRestBreakWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global rest break window steps in order and keeps a trace of each. */
data class GlobalRestBreakWindowEngine(
    var steps: MutableList<GlobalRestBreakWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRestBreakWindow): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRestBreakWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRestBreakWindowEngine(): GlobalRestBreakWindowEngine {
    return GlobalRestBreakWindowEngine(
        steps = mutableListOf<GlobalRestBreakWindowFactor>(
            TitleGlobalRestBreakWindowAdjuster(mutableMapOf<String, Double>("north" to 0.72, "west" to 0.95)),
            TieredGlobalRestBreakWindowAdjuster(
                mutableListOf<Double>(3079.0, 9672.0, 9882.0),
                mutableListOf<Double>(0.99, 1.03, 0.91),
            ),
            MinimumGlobalRestBreakWindowAdjuster(20.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 89309.4,
    )
}
