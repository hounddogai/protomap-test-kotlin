package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling trailer assignment calculation. */
interface RollingTrailerAssignmentStep {
    fun describe(): String
    fun adjust(item: RollingTrailerAssignment, value: Double): Double
}

data class TieredRollingTrailerAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingTrailerAssignmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingTrailerAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingTrailerAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingTrailerAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingTrailerAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingTrailerAssignmentAdjuster(var cap: Double) : RollingTrailerAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingTrailerAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling trailer assignment steps in order and keeps a trace of each. */
data class RollingTrailerAssignmentEngine(
    var steps: MutableList<RollingTrailerAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingTrailerAssignment): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingTrailerAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingTrailerAssignmentEngine(flags: Registry): RollingTrailerAssignmentEngine {
    return RollingTrailerAssignmentEngine(
        steps = mutableListOf<RollingTrailerAssignmentStep>(
            TieredRollingTrailerAssignmentAdjuster(
                mutableListOf<Double>(2379.0, 4144.0, 5867.0, 9600.0),
                mutableListOf<Double>(0.93, 0.94, 0.94, 1.19),
            ),
            FlaggedRollingTrailerAssignmentAdjuster(flags, "rolling-trailer-assignment-primary", -0.9),
            CapRollingTrailerAssignmentAdjuster(16694.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 47374.1,
    )
}
