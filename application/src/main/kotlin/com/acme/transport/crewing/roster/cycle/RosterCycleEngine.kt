package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the roster cycle calculation. */
interface RosterCycleFactor {
    fun describe(): String
    fun adjust(item: RosterCycle, value: Double): Double
}

data class MinimumRosterCycleAdjuster(var minimum: Double) : RosterCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRosterCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRosterCycleAdjuster(var percent: Double) : RosterCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryRosterCycleAdjuster(var factors: MutableMap<String, Double>) : RosterCycleFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: RosterCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the roster cycle steps in order and keeps a trace of each. */
data class RosterCycleEngine(
    var steps: MutableList<RosterCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterCycle): Double {
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

    fun evaluateAll(items: MutableList<RosterCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterCycleEngine(): RosterCycleEngine {
    return RosterCycleEngine(
        steps = mutableListOf<RosterCycleFactor>(
            MinimumRosterCycleAdjuster(8.5),
            TieredRosterCycleAdjuster(
                mutableListOf<Double>(669.0, 3317.0, 9462.0),
                mutableListOf<Double>(1.09, 0.85, 1.09),
            ),
            PercentRosterCycleAdjuster(15.6),
            CategoryRosterCycleAdjuster(mutableMapOf<String, Double>("central" to 0.73, "west" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 53976.9,
    )
}
