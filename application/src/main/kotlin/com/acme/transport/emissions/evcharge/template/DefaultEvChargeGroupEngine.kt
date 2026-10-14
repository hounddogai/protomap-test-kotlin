package com.acme.transport.emissions.evcharge.template

/** One step of the default ev charge group calculation. */
interface DefaultEvChargeGroupStep {
    fun describe(): String
    fun adjust(item: DefaultEvChargeGroup, value: Double): Double
}

data class CategoryDefaultEvChargeGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultEvChargeGroupStep {
    override fun describe(): String = "by category"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultEvChargeGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultEvChargeGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentDefaultEvChargeGroupAdjuster(var percent: Double) : DefaultEvChargeGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultEvChargeGroupLoadKgOffsetAdjuster(var weight: Double) : DefaultEvChargeGroupStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double = value + item.loadKg * weight
}

data class CapDefaultEvChargeGroupAdjuster(var cap: Double) : DefaultEvChargeGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultEvChargeGroupAdjuster(var minimum: Double) : DefaultEvChargeGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultEvChargeGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default ev charge group steps in order and keeps a trace of each. */
data class DefaultEvChargeGroupEngine(
    var steps: MutableList<DefaultEvChargeGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultEvChargeGroup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultEvChargeGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultEvChargeGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultEvChargeGroupEngine(): DefaultEvChargeGroupEngine {
    return DefaultEvChargeGroupEngine(
        steps = mutableListOf<DefaultEvChargeGroupStep>(
            CategoryDefaultEvChargeGroupAdjuster(mutableMapOf<String, Double>("north" to 0.73, "south" to 0.76)),
            TieredDefaultEvChargeGroupAdjuster(
                mutableListOf<Double>(2149.0, 7061.0),
                mutableListOf<Double>(0.96, 1.2),
            ),
            PercentDefaultEvChargeGroupAdjuster(-8.6),
            DefaultEvChargeGroupLoadKgOffsetAdjuster(0.14),
            CapDefaultEvChargeGroupAdjuster(13910.0),
            MinimumDefaultEvChargeGroupAdjuster(16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 69625.3,
    )
}
