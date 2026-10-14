package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped lane plan cycle calculation. */
interface CappedLanePlanCycleStep {
    fun describe(): String
    fun adjust(item: CappedLanePlanCycle, value: Double): Double
}

data class CappedLanePlanCycleDistanceKmOffsetAdjuster(var weight: Double) : CappedLanePlanCycleStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: CappedLanePlanCycle, value: Double): Double = value + item.distanceKm * weight
}

data class PercentCappedLanePlanCycleAdjuster(var percent: Double) : CappedLanePlanCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLanePlanCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCappedLanePlanCycleAdjuster(var factors: MutableMap<String, Double>) : CappedLanePlanCycleStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedLanePlanCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped lane plan cycle steps in order and keeps a trace of each. */
data class CappedLanePlanCycleEngine(
    var steps: MutableList<CappedLanePlanCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedLanePlanCycle): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedLanePlanCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedLanePlanCycleEngine(): CappedLanePlanCycleEngine {
    return CappedLanePlanCycleEngine(
        steps = mutableListOf<CappedLanePlanCycleStep>(
            CappedLanePlanCycleDistanceKmOffsetAdjuster(0.82),
            PercentCappedLanePlanCycleAdjuster(-24.2),
            TitleCappedLanePlanCycleAdjuster(mutableMapOf<String, Double>("south" to 1.23, "east" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 88497.8,
    )
}
