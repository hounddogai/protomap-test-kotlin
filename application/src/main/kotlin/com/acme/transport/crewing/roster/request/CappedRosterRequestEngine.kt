package com.acme.transport.crewing.roster.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped roster request calculation. */
interface CappedRosterRequestAdjuster {
    fun describe(): String
    fun adjust(item: CappedRosterRequest, value: Double): Double
}

data class TieredCappedRosterRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedRosterRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedRosterRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedRosterRequestAdjuster(var percent: Double) : CappedRosterRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedRosterRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedRosterRequestAdjuster(var minimum: Double) : CappedRosterRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedRosterRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameCappedRosterRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedRosterRequestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedRosterRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedRosterRequestEndsAtOffsetAdjuster(var weight: Double) : CappedRosterRequestAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: CappedRosterRequest, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the capped roster request steps in order and keeps a trace of each. */
data class CappedRosterRequestEngine(
    var steps: MutableList<CappedRosterRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedRosterRequest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedRosterRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedRosterRequestEngine(): CappedRosterRequestEngine {
    return CappedRosterRequestEngine(
        steps = mutableListOf<CappedRosterRequestAdjuster>(
            TieredCappedRosterRequestAdjuster(
                mutableListOf<Double>(1751.0, 3914.0, 6951.0),
                mutableListOf<Double>(1.0, 1.16, 1.15),
            ),
            PercentCappedRosterRequestAdjuster(6.4),
            MinimumCappedRosterRequestAdjuster(30.1),
            NameCappedRosterRequestAdjuster(mutableMapOf<String, Double>("east" to 0.93)),
            CappedRosterRequestEndsAtOffsetAdjuster(1.03),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 50933.4,
    )
}
