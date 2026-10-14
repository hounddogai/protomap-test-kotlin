package com.acme.transport.network.transittime.batch

/** One step of the transit time batch calculation. */
interface TransitTimeBatchStep {
    fun describe(): String
    fun adjust(item: TransitTimeBatch, value: Double): Double
}

data class NameTransitTimeBatchAdjuster(var factors: MutableMap<String, Double>) : TransitTimeBatchStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TransitTimeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTransitTimeBatchAdjuster(var cap: Double) : TransitTimeBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeBatch, value: Double): Double = minOf(value, cap)
}

data class TieredTransitTimeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TransitTimeBatchLoadKgOffsetAdjuster(var weight: Double) : TransitTimeBatchStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: TransitTimeBatch, value: Double): Double = value + item.loadKg * weight
}

/** Runs the transit time batch steps in order and keeps a trace of each. */
data class TransitTimeBatchEngine(
    var steps: MutableList<TransitTimeBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeBatch): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeBatchEngine(): TransitTimeBatchEngine {
    return TransitTimeBatchEngine(
        steps = mutableListOf<TransitTimeBatchStep>(
            NameTransitTimeBatchAdjuster(
                mutableMapOf<String, Double>("west" to 1.06, "retail" to 1.34, "online" to 1.1),
            ),
            CapTransitTimeBatchAdjuster(13888.0),
            TieredTransitTimeBatchAdjuster(
                mutableListOf<Double>(2648.0, 4497.0, 7860.0, 9274.0),
                mutableListOf<Double>(0.98, 0.89, 0.91, 1.12),
            ),
            TransitTimeBatchLoadKgOffsetAdjuster(0.35),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 22596.9,
    )
}
