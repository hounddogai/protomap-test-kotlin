package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive lane plan allocation calculation. */
interface AdaptiveLanePlanAllocationFactor {
    fun describe(): String
    fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double
}

data class RoundingAdaptiveLanePlanAllocationAdjuster(var step: Double) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveLanePlanAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveLanePlanAllocationAdjuster(var cap: Double) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double = minOf(value, cap)
}

data class TitleAdaptiveLanePlanAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveLanePlanAllocationAdjuster(var minimum: Double) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveLanePlanAllocationMaximumOffsetAdjuster(
    var weight: Double,
) : AdaptiveLanePlanAllocationFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: AdaptiveLanePlanAllocation, value: Double): Double = value + item.maximum * weight
}

/** Runs the adaptive lane plan allocation steps in order and keeps a trace of each. */
data class AdaptiveLanePlanAllocationEngine(
    var steps: MutableList<AdaptiveLanePlanAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveLanePlanAllocation): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLanePlanAllocationFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLanePlanAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveLanePlanAllocationEngine(flags: Registry): AdaptiveLanePlanAllocationEngine {
    return AdaptiveLanePlanAllocationEngine(
        steps = mutableListOf<AdaptiveLanePlanAllocationFactor>(
            RoundingAdaptiveLanePlanAllocationAdjuster(1.0),
            FlaggedAdaptiveLanePlanAllocationAdjuster(flags, "adaptive-lane-plan-allocation-pending", -7.7),
            CapAdaptiveLanePlanAllocationAdjuster(29774.0),
            TitleAdaptiveLanePlanAllocationAdjuster(mutableMapOf<String, Double>("north" to 1.28)),
            MinimumAdaptiveLanePlanAllocationAdjuster(18.1),
            AdaptiveLanePlanAllocationMaximumOffsetAdjuster(0.42),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 42269.0,
    )
}
