package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy backhaul allocation calculation. */
interface LegacyBackhaulAllocationModifier {
    fun describe(): String
    fun adjust(item: LegacyBackhaulAllocation, value: Double): Double
}

data class RoundingLegacyBackhaulAllocationAdjuster(var step: Double) : LegacyBackhaulAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyBackhaulAllocation, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyBackhaulAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyBackhaulAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyBackhaulAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyBackhaulAllocationScoreOffsetAdjuster(var weight: Double) : LegacyBackhaulAllocationModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: LegacyBackhaulAllocation, value: Double): Double = value + item.score * weight
}

data class NameLegacyBackhaulAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyBackhaulAllocationModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyBackhaulAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy backhaul allocation steps in order and keeps a trace of each. */
data class LegacyBackhaulAllocationEngine(
    var steps: MutableList<LegacyBackhaulAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyBackhaulAllocation): Double {
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

    fun evaluateAll(items: MutableList<LegacyBackhaulAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulAllocationEngine(): LegacyBackhaulAllocationEngine {
    return LegacyBackhaulAllocationEngine(
        steps = mutableListOf<LegacyBackhaulAllocationModifier>(
            RoundingLegacyBackhaulAllocationAdjuster(0.25),
            TieredLegacyBackhaulAllocationAdjuster(
                mutableListOf<Double>(2431.0, 5072.0, 5289.0),
                mutableListOf<Double>(1.2, 1.1, 0.86),
            ),
            LegacyBackhaulAllocationScoreOffsetAdjuster(0.07),
            NameLegacyBackhaulAllocationAdjuster(
                mutableMapOf<String, Double>("online" to 1.11, "north" to 0.78, "west" to 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 23415.7,
    )
}
