package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lane plan window calculation. */
interface LanePlanWindowAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanWindow, value: Double): Double
}

data class LanePlanWindowGraceDaysOffsetAdjuster(var weight: Double) : LanePlanWindowAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: LanePlanWindow, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class TieredLanePlanWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedLanePlanWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanWindowAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lane plan window steps in order and keeps a trace of each. */
data class LanePlanWindowEngine(
    var steps: MutableList<LanePlanWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LanePlanWindow): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLanePlanWindowEngine(flags: Registry): LanePlanWindowEngine {
    return LanePlanWindowEngine(
        steps = mutableListOf<LanePlanWindowAdjuster>(
            LanePlanWindowGraceDaysOffsetAdjuster(1.75),
            TieredLanePlanWindowAdjuster(
                mutableListOf<Double>(3724.0, 8044.0),
                mutableListOf<Double>(1.03, 1.1),
            ),
            FlaggedLanePlanWindowAdjuster(flags, "lane-plan-window-default", 1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 78687.4,
    )
}
