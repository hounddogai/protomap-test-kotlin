package com.acme.transport.lastmile.locker.group

/** One step of the locker baseline calculation. */
interface LockerBaselineModifier {
    fun describe(): String
    fun adjust(item: LockerBaseline, value: Double): Double
}

data class TitleLockerBaselineAdjuster(var factors: MutableMap<String, Double>) : LockerBaselineModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LockerBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LockerBaselineMaximumOffsetAdjuster(var weight: Double) : LockerBaselineModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LockerBaseline, value: Double): Double = value + item.maximum * weight
}

data class CapLockerBaselineAdjuster(var cap: Double) : LockerBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LockerBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredLockerBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the locker baseline steps in order and keeps a trace of each. */
data class LockerBaselineEngine(
    var steps: MutableList<LockerBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerBaseline): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LockerBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerBaselineEngine(): LockerBaselineEngine {
    return LockerBaselineEngine(
        steps = mutableListOf<LockerBaselineModifier>(
            TitleLockerBaselineAdjuster(mutableMapOf<String, Double>("online" to 0.96, "south" to 0.82)),
            LockerBaselineMaximumOffsetAdjuster(1.75),
            CapLockerBaselineAdjuster(24066.0),
            TieredLockerBaselineAdjuster(
                mutableListOf<Double>(2756.0, 7793.0, 8658.0),
                mutableListOf<Double>(1.09, 1.05, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 40596.4,
    )
}
