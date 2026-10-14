package com.acme.transport.network.transittime.timebatch

import com.acme.transport.kit.flags.Registry

/** One step of the tiered transit time batch calculation. */
interface TieredTransitTimeBatchModifier {
    fun describe(): String
    fun adjust(item: TieredTransitTimeBatch, value: Double): Double
}

data class FlaggedTieredTransitTimeBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTransitTimeBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTransitTimeBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredTransitTimeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTransitTimeBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTransitTimeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredTransitTimeBatchQuantityOffsetAdjuster(var weight: Double) : TieredTransitTimeBatchModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TieredTransitTimeBatch, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class CapTieredTransitTimeBatchAdjuster(var cap: Double) : TieredTransitTimeBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTransitTimeBatch, value: Double): Double = minOf(value, cap)
}

data class NameTieredTransitTimeBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTransitTimeBatchModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredTransitTimeBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered transit time batch steps in order and keeps a trace of each. */
data class TieredTransitTimeBatchEngine(
    var steps: MutableList<TieredTransitTimeBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredTransitTimeBatch): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTransitTimeBatchModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTransitTimeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredTransitTimeBatchEngine(flags: Registry): TieredTransitTimeBatchEngine {
    return TieredTransitTimeBatchEngine(
        steps = mutableListOf<TieredTransitTimeBatchModifier>(
            FlaggedTieredTransitTimeBatchAdjuster(flags, "tiered-transit-time-batch-default", -0.8),
            TieredTieredTransitTimeBatchAdjuster(
                mutableListOf<Double>(475.0, 744.0, 6591.0, 8784.0),
                mutableListOf<Double>(1.01, 1.06, 1.09, 1.07),
            ),
            TieredTransitTimeBatchQuantityOffsetAdjuster(1.68),
            CapTieredTransitTimeBatchAdjuster(17099.0),
            NameTieredTransitTimeBatchAdjuster(
                mutableMapOf<String, Double>("south" to 1.24, "retail" to 0.98, "online" to 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 28984.9,
    )
}
