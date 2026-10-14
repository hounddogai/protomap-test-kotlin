package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling lane plan draft calculation. */
interface RollingLanePlanDraftAdjuster {
    fun describe(): String
    fun adjust(item: RollingLanePlanDraft, value: Double): Double
}

data class RollingLanePlanDraftStopCountOffsetAdjuster(var weight: Double) : RollingLanePlanDraftAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RollingLanePlanDraft, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

data class TieredRollingLanePlanDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingLanePlanDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingLanePlanDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotRollingLanePlanDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingLanePlanDraftAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingLanePlanDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingLanePlanDraftAdjuster(var minimum: Double) : RollingLanePlanDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingLanePlanDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling lane plan draft steps in order and keeps a trace of each. */
data class RollingLanePlanDraftEngine(
    var steps: MutableList<RollingLanePlanDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingLanePlanDraft): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingLanePlanDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingLanePlanDraftEngine(): RollingLanePlanDraftEngine {
    return RollingLanePlanDraftEngine(
        steps = mutableListOf<RollingLanePlanDraftAdjuster>(
            RollingLanePlanDraftStopCountOffsetAdjuster(0.89),
            TieredRollingLanePlanDraftAdjuster(
                mutableListOf<Double>(4345.0, 9817.0, 9879.0),
                mutableListOf<Double>(1.02, 1.08, 0.89),
            ),
            DepotRollingLanePlanDraftAdjuster(mutableMapOf<String, Double>("north" to 0.84, "east" to 0.78)),
            MinimumRollingLanePlanDraftAdjuster(5.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 89394.7,
    )
}
