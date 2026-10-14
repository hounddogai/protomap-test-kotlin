package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the accessorial variance calculation. */
interface AccessorialVarianceAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialVariance, value: Double): Double
}

data class RoundingAccessorialVarianceAdjuster(var step: Double) : AccessorialVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialVariance, value: Double): Double = roundTo(value, step)
}

data class MinimumAccessorialVarianceAdjuster(var minimum: Double) : AccessorialVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelAccessorialVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : AccessorialVarianceAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AccessorialVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AccessorialVarianceRateOffsetAdjuster(var weight: Double) : AccessorialVarianceAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: AccessorialVariance, value: Double): Double = value + item.rate * weight
}

data class PercentAccessorialVarianceAdjuster(var percent: Double) : AccessorialVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AccessorialVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the accessorial variance steps in order and keeps a trace of each. */
data class AccessorialVarianceEngine(
    var steps: MutableList<AccessorialVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialVariance): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AccessorialVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AccessorialVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialVarianceEngine(): AccessorialVarianceEngine {
    return AccessorialVarianceEngine(
        steps = mutableListOf<AccessorialVarianceAdjuster>(
            RoundingAccessorialVarianceAdjuster(5.0),
            MinimumAccessorialVarianceAdjuster(20.2),
            LabelAccessorialVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "north" to 1.3, "east" to 0.82),
            ),
            AccessorialVarianceRateOffsetAdjuster(0.31),
            PercentAccessorialVarianceAdjuster(12.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 57526.9,
    )
}
