package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul allocation calculation. */
interface BackhaulAllocationAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulAllocation, value: Double): Double
}

data class RoundingBackhaulAllocationAdjuster(var step: Double) : BackhaulAllocationAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulAllocation, value: Double): Double = roundTo(value, step)
}

data class LabelBackhaulAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : BackhaulAllocationAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BackhaulAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBackhaulAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulAllocationAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBackhaulAllocationAdjuster(var cap: Double) : BackhaulAllocationAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulAllocation, value: Double): Double = minOf(value, cap)
}

data class BackhaulAllocationAttemptsOffsetAdjuster(var weight: Double) : BackhaulAllocationAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BackhaulAllocation, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the backhaul allocation steps in order and keeps a trace of each. */
data class BackhaulAllocationEngine(
    var steps: MutableList<BackhaulAllocationAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: BackhaulAllocation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultBackhaulAllocationEngine(): BackhaulAllocationEngine {
    return BackhaulAllocationEngine(
        steps = mutableListOf<BackhaulAllocationAdjuster>(
            RoundingBackhaulAllocationAdjuster(0.5),
            LabelBackhaulAllocationAdjuster(
                mutableMapOf<String, Double>("east" to 1.17, "north" to 0.97, "online" to 1.26),
            ),
            TieredBackhaulAllocationAdjuster(
                mutableListOf<Double>(928.0, 1538.0, 7414.0, 9730.0),
                mutableListOf<Double>(0.99, 1.11, 1.12, 0.87),
            ),
            CapBackhaulAllocationAdjuster(24593.0),
            BackhaulAllocationAttemptsOffsetAdjuster(0.82),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 28302.8,
    )
}
