package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual lane plan target calculation. */
interface ManualLanePlanTargetAdjuster {
    fun describe(): String
    fun adjust(item: ManualLanePlanTarget, value: Double): Double
}

data class CapManualLanePlanTargetAdjuster(var cap: Double) : ManualLanePlanTargetAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualLanePlanTarget, value: Double): Double = minOf(value, cap)
}

data class PercentManualLanePlanTargetAdjuster(var percent: Double) : ManualLanePlanTargetAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualLanePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualLanePlanTargetAdjuster(var step: Double) : ManualLanePlanTargetAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualLanePlanTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualLanePlanTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualLanePlanTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualLanePlanTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual lane plan target steps in order and keeps a trace of each. */
data class ManualLanePlanTargetEngine(
    var steps: MutableList<ManualLanePlanTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualLanePlanTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualLanePlanTargetAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualLanePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualLanePlanTargetEngine(flags: Registry): ManualLanePlanTargetEngine {
    return ManualLanePlanTargetEngine(
        steps = mutableListOf<ManualLanePlanTargetAdjuster>(
            CapManualLanePlanTargetAdjuster(25711.0),
            PercentManualLanePlanTargetAdjuster(-14.9),
            RoundingManualLanePlanTargetAdjuster(1.0),
            FlaggedManualLanePlanTargetAdjuster(flags, "manual-lane-plan-target-lenient", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 90069.2,
    )
}
