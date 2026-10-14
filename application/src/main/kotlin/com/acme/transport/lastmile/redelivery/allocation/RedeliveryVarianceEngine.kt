package com.acme.transport.lastmile.redelivery.allocation

/** One step of the redelivery variance calculation. */
interface RedeliveryVarianceModifier {
    fun describe(): String
    fun adjust(item: RedeliveryVariance, value: Double): Double
}

data class UnitRedeliveryVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : RedeliveryVarianceModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: RedeliveryVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RedeliveryVarianceQuantityOffsetAdjuster(var weight: Double) : RedeliveryVarianceModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: RedeliveryVariance, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapRedeliveryVarianceAdjuster(var cap: Double) : RedeliveryVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryVariance, value: Double): Double = minOf(value, cap)
}

data class PercentRedeliveryVarianceAdjuster(var percent: Double) : RedeliveryVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the redelivery variance steps in order and keeps a trace of each. */
data class RedeliveryVarianceEngine(
    var steps: MutableList<RedeliveryVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RedeliveryVariance): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRedeliveryVarianceEngine(): RedeliveryVarianceEngine {
    return RedeliveryVarianceEngine(
        steps = mutableListOf<RedeliveryVarianceModifier>(
            UnitRedeliveryVarianceAdjuster(
                mutableMapOf<String, Double>("east" to 1.03, "central" to 0.71, "west" to 1.32),
            ),
            RedeliveryVarianceQuantityOffsetAdjuster(0.63),
            CapRedeliveryVarianceAdjuster(40831.0),
            PercentRedeliveryVarianceAdjuster(8.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 54071.6,
    )
}
