package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled fuel surcharge draft calculation. */
interface ScheduledFuelSurchargeDraftStep {
    fun describe(): String
    fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double
}

data class TitleScheduledFuelSurchargeDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledFuelSurchargeDraftStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledFuelSurchargeDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledFuelSurchargeDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledFuelSurchargeDraftAdjuster(var percent: Double) : ScheduledFuelSurchargeDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumScheduledFuelSurchargeDraftAdjuster(var minimum: Double) : ScheduledFuelSurchargeDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ScheduledFuelSurchargeDraftEndsAtOffsetAdjuster(
    var weight: Double,
) : ScheduledFuelSurchargeDraftStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: ScheduledFuelSurchargeDraft, value: Double): Double {
        return value + item.endsAt.toDouble() * weight
    }
}

/** Runs the scheduled fuel surcharge draft steps in order and keeps a trace of each. */
data class ScheduledFuelSurchargeDraftEngine(
    var steps: MutableList<ScheduledFuelSurchargeDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledFuelSurchargeDraft): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledFuelSurchargeDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledFuelSurchargeDraftEngine(): ScheduledFuelSurchargeDraftEngine {
    return ScheduledFuelSurchargeDraftEngine(
        steps = mutableListOf<ScheduledFuelSurchargeDraftStep>(
            TitleScheduledFuelSurchargeDraftAdjuster(
                mutableMapOf<String, Double>("central" to 0.88, "south" to 0.86),
            ),
            TieredScheduledFuelSurchargeDraftAdjuster(
                mutableListOf<Double>(7646.0, 8387.0),
                mutableListOf<Double>(0.96, 1.15),
            ),
            PercentScheduledFuelSurchargeDraftAdjuster(20.3),
            MinimumScheduledFuelSurchargeDraftAdjuster(8.5),
            ScheduledFuelSurchargeDraftEndsAtOffsetAdjuster(1.02),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 55337.0,
    )
}
