package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock notice calculation. */
interface CrossDockNoticeFactor {
    fun describe(): String
    fun adjust(item: CrossDockNotice, value: Double): Double
}

data class SourceCrossDockNoticeAdjuster(var factors: MutableMap<String, Double>) : CrossDockNoticeFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: CrossDockNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCrossDockNoticeAdjuster(var minimum: Double) : CrossDockNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCrossDockNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cross dock notice steps in order and keeps a trace of each. */
data class CrossDockNoticeEngine(
    var steps: MutableList<CrossDockNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockNotice): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockNoticeEngine(): CrossDockNoticeEngine {
    return CrossDockNoticeEngine(
        steps = mutableListOf<CrossDockNoticeFactor>(
            SourceCrossDockNoticeAdjuster(mutableMapOf<String, Double>("east" to 0.87, "north" to 1.31)),
            MinimumCrossDockNoticeAdjuster(47.4),
            TieredCrossDockNoticeAdjuster(
                mutableListOf<Double>(2448.0, 3767.0, 8169.0),
                mutableListOf<Double>(0.88, 1.03, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 81620.4,
    )
}
