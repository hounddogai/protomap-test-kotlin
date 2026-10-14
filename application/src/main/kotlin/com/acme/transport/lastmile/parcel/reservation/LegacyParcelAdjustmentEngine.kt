package com.acme.transport.lastmile.parcel.reservation

/** One step of the legacy parcel adjustment calculation. */
interface LegacyParcelAdjustmentModifier {
    fun describe(): String
    fun adjust(item: LegacyParcelAdjustment, value: Double): Double
}

data class CapLegacyParcelAdjustmentAdjuster(var cap: Double) : LegacyParcelAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyParcelAdjustment, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyParcelAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyParcelAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyParcelAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyParcelAdjustmentAdjuster(var minimum: Double) : LegacyParcelAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyParcelAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyParcelAdjustmentAdjuster(var percent: Double) : LegacyParcelAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyParcelAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelLegacyParcelAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyParcelAdjustmentModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: LegacyParcelAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy parcel adjustment steps in order and keeps a trace of each. */
data class LegacyParcelAdjustmentEngine(
    var steps: MutableList<LegacyParcelAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyParcelAdjustment): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyParcelAdjustmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyParcelAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyParcelAdjustmentEngine(): LegacyParcelAdjustmentEngine {
    return LegacyParcelAdjustmentEngine(
        steps = mutableListOf<LegacyParcelAdjustmentModifier>(
            CapLegacyParcelAdjustmentAdjuster(35624.0),
            TieredLegacyParcelAdjustmentAdjuster(
                mutableListOf<Double>(2215.0, 8240.0),
                mutableListOf<Double>(1.08, 0.85),
            ),
            MinimumLegacyParcelAdjustmentAdjuster(22.9),
            PercentLegacyParcelAdjustmentAdjuster(13.0),
            ChannelLegacyParcelAdjustmentAdjuster(mutableMapOf<String, Double>("south" to 1.34)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 76059.2,
    )
}
