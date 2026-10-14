package com.acme.transport.linehaul.backhaul.backhaulassignment

/** One step of the strict backhaul override calculation. */
interface StrictBackhaulOverrideFactor {
    fun describe(): String
    fun adjust(item: StrictBackhaulOverride, value: Double): Double
}

data class SourceStrictBackhaulOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictBackhaulOverrideFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: StrictBackhaulOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class StrictBackhaulOverrideFuelLitersOffsetAdjuster(var weight: Double) : StrictBackhaulOverrideFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: StrictBackhaulOverride, value: Double): Double = value + item.fuelLiters * weight
}

data class CapStrictBackhaulOverrideAdjuster(var cap: Double) : StrictBackhaulOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictBackhaulOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the strict backhaul override steps in order and keeps a trace of each. */
data class StrictBackhaulOverrideEngine(
    var steps: MutableList<StrictBackhaulOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictBackhaulOverride): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictBackhaulOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictBackhaulOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictBackhaulOverrideEngine(): StrictBackhaulOverrideEngine {
    return StrictBackhaulOverrideEngine(
        steps = mutableListOf<StrictBackhaulOverrideFactor>(
            SourceStrictBackhaulOverrideAdjuster(mutableMapOf<String, Double>("central" to 0.99)),
            StrictBackhaulOverrideFuelLitersOffsetAdjuster(1.19),
            CapStrictBackhaulOverrideAdjuster(11547.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 11529.2,
    )
}
