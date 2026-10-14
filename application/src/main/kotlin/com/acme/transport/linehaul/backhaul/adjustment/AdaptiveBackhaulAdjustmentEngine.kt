package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive backhaul adjustment calculation. */
interface AdaptiveBackhaulAdjustmentAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double
}

data class PercentAdaptiveBackhaulAdjustmentAdjuster(var percent: Double) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveBackhaulAdjustmentAdjuster(var step: Double) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveBackhaulAdjustmentAdjuster(var cap: Double) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveBackhaulAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelAdaptiveBackhaulAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAdaptiveBackhaulAdjustmentAdjuster(var minimum: Double) : AdaptiveBackhaulAdjustmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveBackhaulAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive backhaul adjustment steps in order and keeps a trace of each. */
data class AdaptiveBackhaulAdjustmentEngine(
    var steps: MutableList<AdaptiveBackhaulAdjustmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveBackhaulAdjustment): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveBackhaulAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveBackhaulAdjustmentEngine(flags: Registry): AdaptiveBackhaulAdjustmentEngine {
    return AdaptiveBackhaulAdjustmentEngine(
        steps = mutableListOf<AdaptiveBackhaulAdjustmentAdjuster>(
            PercentAdaptiveBackhaulAdjustmentAdjuster(9.5),
            RoundingAdaptiveBackhaulAdjustmentAdjuster(0.05),
            CapAdaptiveBackhaulAdjustmentAdjuster(17735.0),
            FlaggedAdaptiveBackhaulAdjustmentAdjuster(flags, "adaptive-backhaul-adjustment-strict", -3.1),
            LabelAdaptiveBackhaulAdjustmentAdjuster(mutableMapOf<String, Double>("north" to 1.0)),
            MinimumAdaptiveBackhaulAdjustmentAdjuster(28.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 65320.0,
    )
}
