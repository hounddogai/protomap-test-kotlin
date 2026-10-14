package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled hold claim calculation. */
interface ScheduledHoldClaimFactor {
    fun describe(): String
    fun adjust(item: ScheduledHoldClaim, value: Double): Double
}

data class SourceScheduledHoldClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledHoldClaimFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: ScheduledHoldClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumScheduledHoldClaimAdjuster(var minimum: Double) : ScheduledHoldClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledHoldClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledHoldClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledHoldClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledHoldClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledHoldClaimAdjuster(var percent: Double) : ScheduledHoldClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledHoldClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled hold claim steps in order and keeps a trace of each. */
data class ScheduledHoldClaimEngine(
    var steps: MutableList<ScheduledHoldClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledHoldClaim): Double {
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

    fun evaluateAll(items: MutableList<ScheduledHoldClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledHoldClaimEngine(): ScheduledHoldClaimEngine {
    return ScheduledHoldClaimEngine(
        steps = mutableListOf<ScheduledHoldClaimFactor>(
            SourceScheduledHoldClaimAdjuster(mutableMapOf<String, Double>("central" to 1.31)),
            MinimumScheduledHoldClaimAdjuster(46.2),
            TieredScheduledHoldClaimAdjuster(
                mutableListOf<Double>(4402.0, 7764.0),
                mutableListOf<Double>(1.02, 0.91),
            ),
            PercentScheduledHoldClaimAdjuster(-22.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 45295.6,
    )
}
