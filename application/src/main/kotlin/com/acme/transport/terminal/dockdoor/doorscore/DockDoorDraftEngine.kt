package com.acme.transport.terminal.dockdoor.doorscore

/** One step of the dock door draft calculation. */
interface DockDoorDraftStep {
    fun describe(): String
    fun adjust(item: DockDoorDraft, value: Double): Double
}

data class TieredDockDoorDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDockDoorDraftAdjuster(var cap: Double) : DockDoorDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorDraft, value: Double): Double = minOf(value, cap)
}

data class MinimumDockDoorDraftAdjuster(var minimum: Double) : DockDoorDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door draft steps in order and keeps a trace of each. */
data class DockDoorDraftEngine(
    var steps: MutableList<DockDoorDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DockDoorDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<DockDoorDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorDraftEngine(): DockDoorDraftEngine {
    return DockDoorDraftEngine(
        steps = mutableListOf<DockDoorDraftStep>(
            TieredDockDoorDraftAdjuster(
                mutableListOf<Double>(5166.0, 6219.0, 6815.0),
                mutableListOf<Double>(0.97, 1.1, 0.83),
            ),
            CapDockDoorDraftAdjuster(19263.0),
            MinimumDockDoorDraftAdjuster(20.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 20914.1,
    )
}
