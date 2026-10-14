package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lane plan target calculation. */
interface LanePlanTargetAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanTarget, value: Double): Double
}

data class CapLanePlanTargetAdjuster(var cap: Double) : LanePlanTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanTarget, value: Double): Double = minOf(value, cap)
}

data class LanePlanTargetWindowDaysOffsetAdjuster(var weight: Double) : LanePlanTargetAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: LanePlanTarget, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class RoundingLanePlanTargetAdjuster(var step: Double) : LanePlanTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumLanePlanTargetAdjuster(var minimum: Double) : LanePlanTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lane plan target steps in order and keeps a trace of each. */
data class LanePlanTargetEngine(
    var steps: MutableList<LanePlanTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanTarget): Double {
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

    fun evaluateAll(items: MutableList<LanePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanTargetEngine(): LanePlanTargetEngine {
    return LanePlanTargetEngine(
        steps = mutableListOf<LanePlanTargetAdjuster>(
            CapLanePlanTargetAdjuster(2074.0),
            LanePlanTargetWindowDaysOffsetAdjuster(1.22),
            RoundingLanePlanTargetAdjuster(1.0),
            MinimumLanePlanTargetAdjuster(19.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 50314.8,
    )
}
