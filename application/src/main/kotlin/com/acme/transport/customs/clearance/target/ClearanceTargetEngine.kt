package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance target calculation. */
interface ClearanceTargetFactor {
    fun describe(): String
    fun adjust(item: ClearanceTarget, value: Double): Double
}

data class ClearanceTargetMarginOffsetAdjuster(var weight: Double) : ClearanceTargetFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ClearanceTarget, value: Double): Double = value + item.margin * weight
}

data class CapClearanceTargetAdjuster(var cap: Double) : ClearanceTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedClearanceTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceTargetFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingClearanceTargetAdjuster(var step: Double) : ClearanceTargetFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceTarget, value: Double): Double = roundTo(value, step)
}

/** Runs the clearance target steps in order and keeps a trace of each. */
data class ClearanceTargetEngine(
    var steps: MutableList<ClearanceTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ClearanceTarget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultClearanceTargetEngine(flags: Registry): ClearanceTargetEngine {
    return ClearanceTargetEngine(
        steps = mutableListOf<ClearanceTargetFactor>(
            ClearanceTargetMarginOffsetAdjuster(0.09),
            CapClearanceTargetAdjuster(37307.0),
            FlaggedClearanceTargetAdjuster(flags, "clearance-target-adaptive", 2.4),
            RoundingClearanceTargetAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 52143.2,
    )
}
