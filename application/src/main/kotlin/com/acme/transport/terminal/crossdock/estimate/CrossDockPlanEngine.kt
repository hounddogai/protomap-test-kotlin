package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock plan calculation. */
interface CrossDockPlanStep {
    fun describe(): String
    fun adjust(item: CrossDockPlan, value: Double): Double
}

data class TitleCrossDockPlanAdjuster(var factors: MutableMap<String, Double>) : CrossDockPlanStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CrossDockPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CrossDockPlanMaximumOffsetAdjuster(var weight: Double) : CrossDockPlanStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: CrossDockPlan, value: Double): Double = value + item.maximum * weight
}

data class RoundingCrossDockPlanAdjuster(var step: Double) : CrossDockPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockPlan, value: Double): Double = roundTo(value, step)
}

data class PercentCrossDockPlanAdjuster(var percent: Double) : CrossDockPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCrossDockPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockPlanStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCrossDockPlanAdjuster(var cap: Double) : CrossDockPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the cross dock plan steps in order and keeps a trace of each. */
data class CrossDockPlanEngine(
    var steps: MutableList<CrossDockPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockPlan): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockPlanEngine(flags: Registry): CrossDockPlanEngine {
    return CrossDockPlanEngine(
        steps = mutableListOf<CrossDockPlanStep>(
            TitleCrossDockPlanAdjuster(mutableMapOf<String, Double>("south" to 1.21, "retail" to 1.07)),
            CrossDockPlanMaximumOffsetAdjuster(0.85),
            RoundingCrossDockPlanAdjuster(0.05),
            PercentCrossDockPlanAdjuster(22.4),
            FlaggedCrossDockPlanAdjuster(flags, "cross-dock-plan-manual", 1.1),
            CapCrossDockPlanAdjuster(28552.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 8117.7,
    )
}
