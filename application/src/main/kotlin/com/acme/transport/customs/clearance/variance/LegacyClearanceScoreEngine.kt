package com.acme.transport.customs.clearance.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy clearance score calculation. */
interface LegacyClearanceScoreFactor {
    fun describe(): String
    fun adjust(item: LegacyClearanceScore, value: Double): Double
}

data class LegacyClearanceScoreCreatedAtOffsetAdjuster(var weight: Double) : LegacyClearanceScoreFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: LegacyClearanceScore, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class PercentLegacyClearanceScoreAdjuster(var percent: Double) : LegacyClearanceScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyClearanceScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyClearanceScoreAdjuster(var step: Double) : LegacyClearanceScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyClearanceScore, value: Double): Double = roundTo(value, step)
}

data class CapLegacyClearanceScoreAdjuster(var cap: Double) : LegacyClearanceScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyClearanceScore, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy clearance score steps in order and keeps a trace of each. */
data class LegacyClearanceScoreEngine(
    var steps: MutableList<LegacyClearanceScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyClearanceScore): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyClearanceScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyClearanceScoreEngine(): LegacyClearanceScoreEngine {
    return LegacyClearanceScoreEngine(
        steps = mutableListOf<LegacyClearanceScoreFactor>(
            LegacyClearanceScoreCreatedAtOffsetAdjuster(0.29),
            PercentLegacyClearanceScoreAdjuster(7.5),
            RoundingLegacyClearanceScoreAdjuster(0.25),
            CapLegacyClearanceScoreAdjuster(46384.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 87254.6,
    )
}
