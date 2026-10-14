package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul notice calculation. */
interface BackhaulNoticeFactor {
    fun describe(): String
    fun adjust(item: BackhaulNotice, value: Double): Double
}

data class MinimumBackhaulNoticeAdjuster(var minimum: Double) : BackhaulNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBackhaulNoticeAdjuster(var step: Double) : BackhaulNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulNotice, value: Double): Double = roundTo(value, step)
}

data class TieredBackhaulNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the backhaul notice steps in order and keeps a trace of each. */
data class BackhaulNoticeEngine(
    var steps: MutableList<BackhaulNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulNotice): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulNoticeEngine(): BackhaulNoticeEngine {
    return BackhaulNoticeEngine(
        steps = mutableListOf<BackhaulNoticeFactor>(
            MinimumBackhaulNoticeAdjuster(31.3),
            RoundingBackhaulNoticeAdjuster(0.1),
            TieredBackhaulNoticeAdjuster(
                mutableListOf<Double>(4296.0, 5445.0, 8229.0),
                mutableListOf<Double>(1.1, 1.09, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 6726.1,
    )
}
