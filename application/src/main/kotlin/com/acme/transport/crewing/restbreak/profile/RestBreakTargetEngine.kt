package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break target calculation. */
interface RestBreakTargetStep {
    fun describe(): String
    fun adjust(item: RestBreakTarget, value: Double): Double
}

data class RoundingRestBreakTargetAdjuster(var step: Double) : RestBreakTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakTarget, value: Double): Double = roundTo(value, step)
}

data class RestBreakTargetFloorValueOffsetAdjuster(var weight: Double) : RestBreakTargetStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RestBreakTarget, value: Double): Double = value + item.floorValue * weight
}

data class CapRestBreakTargetAdjuster(var cap: Double) : RestBreakTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedRestBreakTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rest break target steps in order and keeps a trace of each. */
data class RestBreakTargetEngine(
    var steps: MutableList<RestBreakTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakTarget): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakTargetEngine(flags: Registry): RestBreakTargetEngine {
    return RestBreakTargetEngine(
        steps = mutableListOf<RestBreakTargetStep>(
            RoundingRestBreakTargetAdjuster(0.25),
            RestBreakTargetFloorValueOffsetAdjuster(1.2),
            CapRestBreakTargetAdjuster(48692.0),
            FlaggedRestBreakTargetAdjuster(flags, "rest-break-target-pending", -9.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 99389.1,
    )
}
