package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.flags.Registry

/** One step of the pending fuel surcharge variance calculation. */
interface PendingFuelSurchargeVarianceFactor {
    fun describe(): String
    fun adjust(item: PendingFuelSurchargeVariance, value: Double): Double
}

data class FlaggedPendingFuelSurchargeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingFuelSurchargeVarianceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingFuelSurchargeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentPendingFuelSurchargeVarianceAdjuster(
    var percent: Double,
) : PendingFuelSurchargeVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingFuelSurchargeVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingFuelSurchargeVarianceAdjuster(
    var minimum: Double,
) : PendingFuelSurchargeVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingFuelSurchargeVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending fuel surcharge variance steps in order and keeps a trace of each. */
data class PendingFuelSurchargeVarianceEngine(
    var steps: MutableList<PendingFuelSurchargeVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingFuelSurchargeVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingFuelSurchargeVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingFuelSurchargeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingFuelSurchargeVarianceEngine(flags: Registry): PendingFuelSurchargeVarianceEngine {
    return PendingFuelSurchargeVarianceEngine(
        steps = mutableListOf<PendingFuelSurchargeVarianceFactor>(
            FlaggedPendingFuelSurchargeVarianceAdjuster(flags, "pending-fuel-surcharge-variance-batched", 2.0),
            PercentPendingFuelSurchargeVarianceAdjuster(-8.5),
            MinimumPendingFuelSurchargeVarianceAdjuster(40.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 88419.1,
    )
}
