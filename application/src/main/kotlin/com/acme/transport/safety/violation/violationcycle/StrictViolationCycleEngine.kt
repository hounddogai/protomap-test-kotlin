package com.acme.transport.safety.violation.violationcycle

/** One step of the strict violation cycle calculation. */
interface StrictViolationCycleModifier {
    fun describe(): String
    fun adjust(item: StrictViolationCycle, value: Double): Double
}

data class StrictViolationCycleBudgetOffsetAdjuster(var weight: Double) : StrictViolationCycleModifier {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: StrictViolationCycle, value: Double): Double = value + item.budget * weight
}

data class TieredStrictViolationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictViolationCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictViolationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleStrictViolationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictViolationCycleModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: StrictViolationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictViolationCycleAdjuster(var percent: Double) : StrictViolationCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictViolationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictViolationCycleAdjuster(var cap: Double) : StrictViolationCycleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictViolationCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the strict violation cycle steps in order and keeps a trace of each. */
data class StrictViolationCycleEngine(
    var steps: MutableList<StrictViolationCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictViolationCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictViolationCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictViolationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictViolationCycleEngine(): StrictViolationCycleEngine {
    return StrictViolationCycleEngine(
        steps = mutableListOf<StrictViolationCycleModifier>(
            StrictViolationCycleBudgetOffsetAdjuster(0.86),
            TieredStrictViolationCycleAdjuster(
                mutableListOf<Double>(5755.0, 8195.0, 8686.0, 9397.0),
                mutableListOf<Double>(1.13, 0.94, 0.94, 0.99),
            ),
            LocaleStrictViolationCycleAdjuster(
                mutableMapOf<String, Double>("online" to 0.92, "retail" to 0.7, "west" to 1.1),
            ),
            PercentStrictViolationCycleAdjuster(20.4),
            CapStrictViolationCycleAdjuster(17054.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 53310.0,
    )
}
