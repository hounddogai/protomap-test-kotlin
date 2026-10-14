package com.acme.transport.customs.hold.slot

/** One step of the primary hold override calculation. */
interface PrimaryHoldOverrideStep {
    fun describe(): String
    fun adjust(item: PrimaryHoldOverride, value: Double): Double
}

data class PercentPrimaryHoldOverrideAdjuster(var percent: Double) : PrimaryHoldOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryHoldOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryPrimaryHoldOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryHoldOverrideStep {
    override fun describe(): String = "by country"

    override fun adjust(item: PrimaryHoldOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PrimaryHoldOverrideFuelLitersOffsetAdjuster(var weight: Double) : PrimaryHoldOverrideStep {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: PrimaryHoldOverride, value: Double): Double = value + item.fuelLiters * weight
}

data class MinimumPrimaryHoldOverrideAdjuster(var minimum: Double) : PrimaryHoldOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryHoldOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary hold override steps in order and keeps a trace of each. */
data class PrimaryHoldOverrideEngine(
    var steps: MutableList<PrimaryHoldOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryHoldOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryHoldOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryHoldOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryHoldOverrideEngine(): PrimaryHoldOverrideEngine {
    return PrimaryHoldOverrideEngine(
        steps = mutableListOf<PrimaryHoldOverrideStep>(
            PercentPrimaryHoldOverrideAdjuster(9.6),
            CountryPrimaryHoldOverrideAdjuster(mutableMapOf<String, Double>("east" to 1.38, "south" to 0.84)),
            PrimaryHoldOverrideFuelLitersOffsetAdjuster(0.89),
            MinimumPrimaryHoldOverrideAdjuster(22.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 96160.7,
    )
}
