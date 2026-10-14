package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped incident claim calculation. */
interface CappedIncidentClaimFactor {
    fun describe(): String
    fun adjust(item: CappedIncidentClaim, value: Double): Double
}

data class CategoryCappedIncidentClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedIncidentClaimFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedIncidentClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedIncidentClaimAdjuster(var minimum: Double) : CappedIncidentClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedIncidentClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedIncidentClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedIncidentClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedIncidentClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCappedIncidentClaimAdjuster(var cap: Double) : CappedIncidentClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedIncidentClaim, value: Double): Double = minOf(value, cap)
}

/** Runs the capped incident claim steps in order and keeps a trace of each. */
data class CappedIncidentClaimEngine(
    var steps: MutableList<CappedIncidentClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedIncidentClaim): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedIncidentClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedIncidentClaimEngine(): CappedIncidentClaimEngine {
    return CappedIncidentClaimEngine(
        steps = mutableListOf<CappedIncidentClaimFactor>(
            CategoryCappedIncidentClaimAdjuster(mutableMapOf<String, Double>("north" to 0.82, "online" to 1.2)),
            MinimumCappedIncidentClaimAdjuster(44.6),
            TieredCappedIncidentClaimAdjuster(
                mutableListOf<Double>(4633.0, 4736.0, 5510.0, 5842.0),
                mutableListOf<Double>(1.11, 1.04, 0.98, 1.0),
            ),
            CapCappedIncidentClaimAdjuster(1961.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 31437.2,
    )
}
