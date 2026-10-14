package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped lane plan target calculation. */
interface CappedLanePlanTargetFactor {
    fun describe(): String
    fun adjust(item: CappedLanePlanTarget, value: Double): Double
}

data class RoundingCappedLanePlanTargetAdjuster(var step: Double) : CappedLanePlanTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedLanePlanTarget, value: Double): Double = roundTo(value, step)
}

data class LabelCappedLanePlanTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedLanePlanTargetFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedLanePlanTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedLanePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedLanePlanTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedLanePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedLanePlanTargetAdjuster(var percent: Double) : CappedLanePlanTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLanePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped lane plan target steps in order and keeps a trace of each. */
data class CappedLanePlanTargetEngine(
    var steps: MutableList<CappedLanePlanTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedLanePlanTarget): Double {
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

    fun evaluateAll(items: MutableList<CappedLanePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedLanePlanTargetEngine(flags: Registry): CappedLanePlanTargetEngine {
    return CappedLanePlanTargetEngine(
        steps = mutableListOf<CappedLanePlanTargetFactor>(
            RoundingCappedLanePlanTargetAdjuster(1.0),
            LabelCappedLanePlanTargetAdjuster(
                mutableMapOf<String, Double>("central" to 1.15, "online" to 0.9, "north" to 1.38),
            ),
            FlaggedCappedLanePlanTargetAdjuster(flags, "capped-lane-plan-target-tiered", -4.5),
            PercentCappedLanePlanTargetAdjuster(5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 74124.5,
    )
}
