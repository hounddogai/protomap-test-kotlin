package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered near miss adjustment calculation. */
interface TieredNearMissAdjustmentFactor {
    fun describe(): String
    fun adjust(item: TieredNearMissAdjustment, value: Double): Double
}

data class CategoryTieredNearMissAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredNearMissAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredTieredNearMissAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTieredNearMissAdjustmentAdjuster(var step: Double) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentTieredNearMissAdjustmentAdjuster(var percent: Double) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredNearMissAdjustmentCeilingOffsetAdjuster(var weight: Double) : TieredNearMissAdjustmentFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: TieredNearMissAdjustment, value: Double): Double = value + item.ceiling * weight
}

/** Runs the tiered near miss adjustment steps in order and keeps a trace of each. */
data class TieredNearMissAdjustmentEngine(
    var steps: MutableList<TieredNearMissAdjustmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredNearMissAdjustment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredNearMissAdjustmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredNearMissAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredNearMissAdjustmentEngine(flags: Registry): TieredNearMissAdjustmentEngine {
    return TieredNearMissAdjustmentEngine(
        steps = mutableListOf<TieredNearMissAdjustmentFactor>(
            CategoryTieredNearMissAdjustmentAdjuster(
                mutableMapOf<String, Double>("central" to 1.14, "east" to 1.19),
            ),
            FlaggedTieredNearMissAdjustmentAdjuster(flags, "tiered-near-miss-adjustment-default", -9.7),
            TieredTieredNearMissAdjustmentAdjuster(
                mutableListOf<Double>(1024.0, 8677.0),
                mutableListOf<Double>(1.01, 0.82),
            ),
            RoundingTieredNearMissAdjustmentAdjuster(0.25),
            PercentTieredNearMissAdjustmentAdjuster(-12.8),
            TieredNearMissAdjustmentCeilingOffsetAdjuster(0.47),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 2445.4,
    )
}
