package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the near miss cycle calculation. */
interface NearMissCycleFactor {
    fun describe(): String
    fun adjust(item: NearMissCycle, value: Double): Double
}

data class LaneNearMissCycleAdjuster(var factors: MutableMap<String, Double>) : NearMissCycleFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: NearMissCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingNearMissCycleAdjuster(var step: Double) : NearMissCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: NearMissCycle, value: Double): Double = roundTo(value, step)
}

data class PercentNearMissCycleAdjuster(var percent: Double) : NearMissCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapNearMissCycleAdjuster(var cap: Double) : NearMissCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the near miss cycle steps in order and keeps a trace of each. */
data class NearMissCycleEngine(
    var steps: MutableList<NearMissCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: NearMissCycle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultNearMissCycleEngine(): NearMissCycleEngine {
    return NearMissCycleEngine(
        steps = mutableListOf<NearMissCycleFactor>(
            LaneNearMissCycleAdjuster(mutableMapOf<String, Double>("east" to 0.81, "north" to 1.2)),
            RoundingNearMissCycleAdjuster(0.05),
            PercentNearMissCycleAdjuster(0.5),
            CapNearMissCycleAdjuster(28354.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 48464.9,
    )
}
