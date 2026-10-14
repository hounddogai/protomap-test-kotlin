package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan profile calculation. */
interface LanePlanProfileStep {
    fun describe(): String
    fun adjust(item: LanePlanProfile, value: Double): Double
}

data class RoundingLanePlanProfileAdjuster(var step: Double) : LanePlanProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanProfile, value: Double): Double = roundTo(value, step)
}

data class LanePlanProfileCostOffsetAdjuster(var weight: Double) : LanePlanProfileStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LanePlanProfile, value: Double): Double = value + item.cost * weight
}

data class LabelLanePlanProfileAdjuster(var factors: MutableMap<String, Double>) : LanePlanProfileStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LanePlanProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lane plan profile steps in order and keeps a trace of each. */
data class LanePlanProfileEngine(
    var steps: MutableList<LanePlanProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanProfile): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanProfileEngine(): LanePlanProfileEngine {
    return LanePlanProfileEngine(
        steps = mutableListOf<LanePlanProfileStep>(
            RoundingLanePlanProfileAdjuster(0.1),
            LanePlanProfileCostOffsetAdjuster(1.78),
            LabelLanePlanProfileAdjuster(mutableMapOf<String, Double>("west" to 0.82, "online" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 85438.4,
    )
}
