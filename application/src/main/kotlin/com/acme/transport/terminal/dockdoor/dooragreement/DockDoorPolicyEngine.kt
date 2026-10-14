package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.numbers.roundTo

/** One step of the dock door policy calculation. */
interface DockDoorPolicyStep {
    fun describe(): String
    fun adjust(item: DockDoorPolicy, value: Double): Double
}

data class RoundingDockDoorPolicyAdjuster(var step: Double) : DockDoorPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DockDoorPolicy, value: Double): Double = roundTo(value, step)
}

data class TieredDockDoorPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDockDoorPolicyAdjuster(var cap: Double) : DockDoorPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorPolicy, value: Double): Double = minOf(value, cap)
}

data class DockDoorPolicyThresholdOffsetAdjuster(var weight: Double) : DockDoorPolicyStep {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: DockDoorPolicy, value: Double): Double = value + item.threshold * weight
}

data class PercentDockDoorPolicyAdjuster(var percent: Double) : DockDoorPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DockDoorPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotDockDoorPolicyAdjuster(var factors: MutableMap<String, Double>) : DockDoorPolicyStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: DockDoorPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dock door policy steps in order and keeps a trace of each. */
data class DockDoorPolicyEngine(
    var steps: MutableList<DockDoorPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorPolicy): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorPolicyEngine(): DockDoorPolicyEngine {
    return DockDoorPolicyEngine(
        steps = mutableListOf<DockDoorPolicyStep>(
            RoundingDockDoorPolicyAdjuster(0.25),
            TieredDockDoorPolicyAdjuster(
                mutableListOf<Double>(7852.0, 8712.0),
                mutableListOf<Double>(1.16, 1.01),
            ),
            CapDockDoorPolicyAdjuster(42796.0),
            DockDoorPolicyThresholdOffsetAdjuster(1.68),
            PercentDockDoorPolicyAdjuster(20.6),
            DepotDockDoorPolicyAdjuster(mutableMapOf<String, Double>("west" to 1.12, "east" to 0.82)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 59505.9,
    )
}
