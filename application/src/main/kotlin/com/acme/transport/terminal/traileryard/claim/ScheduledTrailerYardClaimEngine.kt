package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled trailer yard claim calculation. */
interface ScheduledTrailerYardClaimFactor {
    fun describe(): String
    fun adjust(item: ScheduledTrailerYardClaim, value: Double): Double
}

data class MinimumScheduledTrailerYardClaimAdjuster(var minimum: Double) : ScheduledTrailerYardClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledTrailerYardClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledTrailerYardClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTrailerYardClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTrailerYardClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledTrailerYardClaimWindowDaysOffsetAdjuster(
    var weight: Double,
) : ScheduledTrailerYardClaimFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: ScheduledTrailerYardClaim, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the scheduled trailer yard claim steps in order and keeps a trace of each. */
data class ScheduledTrailerYardClaimEngine(
    var steps: MutableList<ScheduledTrailerYardClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledTrailerYardClaim): Double {
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

    fun evaluateAll(items: MutableList<ScheduledTrailerYardClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledTrailerYardClaimEngine(): ScheduledTrailerYardClaimEngine {
    return ScheduledTrailerYardClaimEngine(
        steps = mutableListOf<ScheduledTrailerYardClaimFactor>(
            MinimumScheduledTrailerYardClaimAdjuster(25.3),
            TieredScheduledTrailerYardClaimAdjuster(
                mutableListOf<Double>(3929.0, 6110.0),
                mutableListOf<Double>(0.9, 1.06),
            ),
            ScheduledTrailerYardClaimWindowDaysOffsetAdjuster(0.86),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 86325.9,
    )
}
