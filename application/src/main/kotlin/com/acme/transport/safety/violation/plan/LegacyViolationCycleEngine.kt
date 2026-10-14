package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy violation cycle calculation. */
interface LegacyViolationCycleFactor {
    fun describe(): String
    fun adjust(item: LegacyViolationCycle, value: Double): Double
}

data class LegacyViolationCycleCapacityOffsetAdjuster(var weight: Double) : LegacyViolationCycleFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: LegacyViolationCycle, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class RoundingLegacyViolationCycleAdjuster(var step: Double) : LegacyViolationCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyViolationCycle, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyViolationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyViolationCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyViolationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyViolationCycleAdjuster(var cap: Double) : LegacyViolationCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyViolationCycle, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyViolationCycleAdjuster(var percent: Double) : LegacyViolationCycleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyViolationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy violation cycle steps in order and keeps a trace of each. */
data class LegacyViolationCycleEngine(
    var steps: MutableList<LegacyViolationCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyViolationCycle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyViolationCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyViolationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyViolationCycleEngine(): LegacyViolationCycleEngine {
    return LegacyViolationCycleEngine(
        steps = mutableListOf<LegacyViolationCycleFactor>(
            LegacyViolationCycleCapacityOffsetAdjuster(0.64),
            RoundingLegacyViolationCycleAdjuster(0.1),
            TieredLegacyViolationCycleAdjuster(
                mutableListOf<Double>(2300.0, 4984.0, 5124.0, 7811.0),
                mutableListOf<Double>(0.82, 0.91, 0.97, 1.06),
            ),
            CapLegacyViolationCycleAdjuster(7752.0),
            PercentLegacyViolationCycleAdjuster(-16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 26562.9,
    )
}
