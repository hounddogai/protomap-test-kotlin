package com.acme.transport.linehaul.backhaul.backhaulassignment

/** One step of the pending backhaul mapping calculation. */
interface PendingBackhaulMappingStep {
    fun describe(): String
    fun adjust(item: PendingBackhaulMapping, value: Double): Double
}

data class PendingBackhaulMappingCostOffsetAdjuster(var weight: Double) : PendingBackhaulMappingStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: PendingBackhaulMapping, value: Double): Double = value + item.cost * weight
}

data class TieredPendingBackhaulMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingBackhaulMappingStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingBackhaulMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPendingBackhaulMappingAdjuster(var minimum: Double) : PendingBackhaulMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingBackhaulMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingBackhaulMappingAdjuster(var percent: Double) : PendingBackhaulMappingStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingBackhaulMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending backhaul mapping steps in order and keeps a trace of each. */
data class PendingBackhaulMappingEngine(
    var steps: MutableList<PendingBackhaulMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingBackhaulMapping): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingBackhaulMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingBackhaulMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingBackhaulMappingEngine(): PendingBackhaulMappingEngine {
    return PendingBackhaulMappingEngine(
        steps = mutableListOf<PendingBackhaulMappingStep>(
            PendingBackhaulMappingCostOffsetAdjuster(0.44),
            TieredPendingBackhaulMappingAdjuster(
                mutableListOf<Double>(3902.0, 4514.0, 7520.0, 7712.0),
                mutableListOf<Double>(0.84, 0.97, 0.97, 0.91),
            ),
            MinimumPendingBackhaulMappingAdjuster(23.0),
            PercentPendingBackhaulMappingAdjuster(-6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 31594.7,
    )
}
