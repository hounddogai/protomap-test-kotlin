package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation group calculation. */
interface ViolationGroupFactor {
    fun describe(): String
    fun adjust(item: ViolationGroup, value: Double): Double
}

data class TieredViolationGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ViolationGroupDurationMinutesOffsetAdjuster(var weight: Double) : ViolationGroupFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ViolationGroup, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class RoundingViolationGroupAdjuster(var step: Double) : ViolationGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the violation group steps in order and keeps a trace of each. */
data class ViolationGroupEngine(
    var steps: MutableList<ViolationGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationGroup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationGroupEngine(): ViolationGroupEngine {
    return ViolationGroupEngine(
        steps = mutableListOf<ViolationGroupFactor>(
            TieredViolationGroupAdjuster(
                mutableListOf<Double>(652.0, 2978.0, 8913.0),
                mutableListOf<Double>(1.08, 1.08, 1.05),
            ),
            ViolationGroupDurationMinutesOffsetAdjuster(0.93),
            RoundingViolationGroupAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 30716.9,
    )
}
