package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.flags.Registry

/** One step of the weighted fuel surcharge notice calculation. */
interface WeightedFuelSurchargeNoticeStep {
    fun describe(): String
    fun adjust(item: WeightedFuelSurchargeNotice, value: Double): Double
}

data class MinimumWeightedFuelSurchargeNoticeAdjuster(var minimum: Double) : WeightedFuelSurchargeNoticeStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelSurchargeNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedWeightedFuelSurchargeNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelSurchargeNoticeStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelSurchargeNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapWeightedFuelSurchargeNoticeAdjuster(var cap: Double) : WeightedFuelSurchargeNoticeStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFuelSurchargeNotice, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted fuel surcharge notice steps in order and keeps a trace of each. */
data class WeightedFuelSurchargeNoticeEngine(
    var steps: MutableList<WeightedFuelSurchargeNoticeStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedFuelSurchargeNotice): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedFuelSurchargeNoticeStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedFuelSurchargeNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedFuelSurchargeNoticeEngine(flags: Registry): WeightedFuelSurchargeNoticeEngine {
    return WeightedFuelSurchargeNoticeEngine(
        steps = mutableListOf<WeightedFuelSurchargeNoticeStep>(
            MinimumWeightedFuelSurchargeNoticeAdjuster(35.4),
            FlaggedWeightedFuelSurchargeNoticeAdjuster(flags, "weighted-fuel-surcharge-notice-fallback", 5.0),
            CapWeightedFuelSurchargeNoticeAdjuster(6744.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 77095.5,
    )
}
