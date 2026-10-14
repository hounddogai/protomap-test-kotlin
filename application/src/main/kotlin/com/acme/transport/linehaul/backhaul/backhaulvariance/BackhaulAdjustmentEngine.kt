package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul adjustment calculation. */
interface BackhaulAdjustmentModifier {
    fun describe(): String
    fun adjust(item: BackhaulAdjustment, value: Double): Double
}

data class FlaggedBackhaulAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBackhaulAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBackhaulAdjustmentAdjuster(var percent: Double) : BackhaulAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapBackhaulAdjustmentAdjuster(var cap: Double) : BackhaulAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double = minOf(value, cap)
}

data class MinimumBackhaulAdjustmentAdjuster(var minimum: Double) : BackhaulAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class BackhaulAdjustmentCeilingOffsetAdjuster(var weight: Double) : BackhaulAdjustmentModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: BackhaulAdjustment, value: Double): Double = value + item.ceiling * weight
}

/** Runs the backhaul adjustment steps in order and keeps a trace of each. */
data class BackhaulAdjustmentEngine(
    var steps: MutableList<BackhaulAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulAdjustment): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulAdjustmentEngine(flags: Registry): BackhaulAdjustmentEngine {
    return BackhaulAdjustmentEngine(
        steps = mutableListOf<BackhaulAdjustmentModifier>(
            FlaggedBackhaulAdjustmentAdjuster(flags, "backhaul-adjustment-capped", 8.7),
            TieredBackhaulAdjustmentAdjuster(
                mutableListOf<Double>(7887.0, 8729.0),
                mutableListOf<Double>(0.8, 1.2),
            ),
            PercentBackhaulAdjustmentAdjuster(-16.4),
            CapBackhaulAdjustmentAdjuster(1740.0),
            MinimumBackhaulAdjustmentAdjuster(29.0),
            BackhaulAdjustmentCeilingOffsetAdjuster(1.44),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 2178.8,
    )
}
