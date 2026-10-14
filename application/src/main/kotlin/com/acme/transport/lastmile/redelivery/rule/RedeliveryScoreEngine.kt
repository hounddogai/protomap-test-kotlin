package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the redelivery score calculation. */
interface RedeliveryScoreFactor {
    fun describe(): String
    fun adjust(item: RedeliveryScore, value: Double): Double
}

data class CapRedeliveryScoreAdjuster(var cap: Double) : RedeliveryScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryScore, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryScoreAdjuster(var percent: Double) : RedeliveryScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRedeliveryScoreAdjuster(var minimum: Double) : RedeliveryScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleRedeliveryScoreAdjuster(var factors: MutableMap<String, Double>) : RedeliveryScoreFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: RedeliveryScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRedeliveryScoreAdjuster(var step: Double) : RedeliveryScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RedeliveryScore, value: Double): Double = roundTo(value, step)
}

/** Runs the redelivery score steps in order and keeps a trace of each. */
data class RedeliveryScoreEngine(
    var steps: MutableList<RedeliveryScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RedeliveryScore): Double {
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

    fun evaluateAll(items: MutableList<RedeliveryScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRedeliveryScoreEngine(): RedeliveryScoreEngine {
    return RedeliveryScoreEngine(
        steps = mutableListOf<RedeliveryScoreFactor>(
            CapRedeliveryScoreAdjuster(33852.0),
            PercentRedeliveryScoreAdjuster(-7.9),
            MinimumRedeliveryScoreAdjuster(10.5),
            TitleRedeliveryScoreAdjuster(mutableMapOf<String, Double>("online" to 1.27, "north" to 0.81)),
            RoundingRedeliveryScoreAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 10544.4,
    )
}
