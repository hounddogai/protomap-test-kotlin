package com.acme.transport.lastmile.locker.preference

/** One step of the locker exception calculation. */
interface LockerExceptionModifier {
    fun describe(): String
    fun adjust(item: LockerException, value: Double): Double
}

data class MinimumLockerExceptionAdjuster(var minimum: Double) : LockerExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLockerExceptionAdjuster(var percent: Double) : LockerExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLockerExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameLockerExceptionAdjuster(var factors: MutableMap<String, Double>) : LockerExceptionModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LockerException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LockerExceptionCapacityOffsetAdjuster(var weight: Double) : LockerExceptionModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: LockerException, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the locker exception steps in order and keeps a trace of each. */
data class LockerExceptionEngine(
    var steps: MutableList<LockerExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LockerException): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LockerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLockerExceptionEngine(): LockerExceptionEngine {
    return LockerExceptionEngine(
        steps = mutableListOf<LockerExceptionModifier>(
            MinimumLockerExceptionAdjuster(29.5),
            PercentLockerExceptionAdjuster(7.3),
            TieredLockerExceptionAdjuster(
                mutableListOf<Double>(161.0, 2624.0, 6383.0, 6817.0),
                mutableListOf<Double>(0.9, 0.93, 0.89, 1.07),
            ),
            NameLockerExceptionAdjuster(mutableMapOf<String, Double>("central" to 1.35)),
            LockerExceptionCapacityOffsetAdjuster(1.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 56710.6,
    )
}
